package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.mapper.OaFormsMapper;
import xyz.erupt.flow.service.FormsService;
import xyz.erupt.flow.service.ProcessDefinitionService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FormsServiceImpl extends ServiceImpl<OaFormsMapper, OaForms> implements FormsService {

    @Lazy
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(OaForms entity) {
        entity.setIsStop(entity.getGroupId()==-1);
        //需要同时修改对应的流程定义的锁定状态和分组
        OaProcessDefinition build = OaProcessDefinition.builder()
                .isStop(entity.getIsStop())
                .groupId(entity.getGroupId())
                .build();
        processDefinitionService.updateByFormId(build, entity.getFormId());
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createForm(OaForms form) {
        Date now = new Date();
        form.setSort(0);
        form.setIsStop(false);
        form.setCreated(now);
        form.setUpdated(now);
        super.save(form);//保存表单
        processDefinitionService.deploy(form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFormDetail(OaForms forms) {
        processDefinitionService.deploy(forms);
        //不更新的字段设为null
        forms.setSort(null);
        forms.setIsStop(null);
        forms.setUpdated(new Date());
        super.updateById(forms);
    }

    @Override
    public List<OaForms> listByGroupId(Long groupId, String keywords) {
        QueryWrapper<OaForms> queryWrapper = new QueryWrapper<>(OaForms.builder()
                .groupId(groupId)
                .build()).orderByAsc("sort");
        if(StringUtils.isNotEmpty(keywords)) {
            queryWrapper.lambda().like(OaForms::getFormName, keywords);
        }
        return super.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void formsSort(List<Long> formIds) {
        List<OaForms> updateList = new ArrayList<>();
        for (int i1 = 0; i1 < formIds.size(); i1++) {
            updateList.add(
                    OaForms.builder()
                            .formId(formIds.get(i1))
                            .sort(i1)
                            .build()
            );
        }
        super.updateBatchById(updateList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        //级联删除流程定义
        processDefinitionService.removeByFormId((Long) id);
        return super.removeById(id);
    }
}
