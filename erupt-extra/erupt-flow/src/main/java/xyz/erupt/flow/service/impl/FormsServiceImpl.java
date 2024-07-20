package xyz.erupt.flow.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.service.FormsService;
import xyz.erupt.flow.service.ProcessDefinitionService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FormsServiceImpl implements FormsService {

    @Lazy
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public void updateById(OaForms entity) {
        entity.setIsStop(entity.getGroupId() == -1);
        //需要同时修改对应的流程定义的锁定状态和分组
        OaProcessDefinition lastVersionByFromId = processDefinitionService.getLastVersionByFromId(entity.getFormId());
        lastVersionByFromId.setIsStop(entity.getIsStop());
        lastVersionByFromId.setGroupId(entity.getGroupId());
        eruptDao.merge(lastVersionByFromId);
        eruptDao.merge(entity);
    }

    @Override
    @Transactional
    public void createForm(OaForms form) {
        Date now = new Date();
        form.setSort(0);
        form.setIsStop(false);
        form.setCreated(now);
        form.setUpdated(now);
        eruptDao.persist(form);
        processDefinitionService.deploy(form);
    }

    @Override
    @Transactional
    public void updateFormDetail(OaForms forms) {
        processDefinitionService.deploy(forms);
        //不更新的字段设为null
        forms.setSort(null);
        forms.setIsStop(null);
        forms.setUpdated(new Date());
        eruptDao.merge(forms);
    }

    @Override
    public List<OaForms> listByGroupId(Long groupId, String keywords) {
        return eruptDao.lambdaQuery(OaForms.class).eq(OaForms::getGroupId, groupId)
                .orderBy(OaForms::getSort)
                .like(StringUtils.isNotEmpty(keywords), OaForms::getFormName, keywords)
                .list();
    }

    @Override
    @Transactional
    public void formsSort(List<Long> formIds) {
        for (int i1 = 0; i1 < formIds.size(); i1++) {
            OaForms oaForms = OaForms.builder().formId(formIds.get(i1)).sort(i1).build();
            eruptDao.merge(oaForms);
        }
    }

    @Override
    @Transactional
    public void removeById(Serializable id) {
        //级联删除流程定义
        processDefinitionService.removeByFormId((Long) id);
        eruptDao.delete(OaForms.builder().formId((Long) id).build());
    }
}
