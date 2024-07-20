package xyz.erupt.flow.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.flow.bean.entity.OaFormGroups;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.service.FormGroupService;
import xyz.erupt.flow.service.FormsService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author : willian fu
 * date : 2022/7/4
 */
@Slf4j
@Service
public class FormGroupServiceImpl implements FormGroupService {

    @Resource
    private FormsService formsService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public List<OaFormGroups> getFormGroups(OaFormGroups formGroupVo) {
        List<OaFormGroups> voList = this.getFormGroupList();
        //循环添加组内的表单
        voList.forEach(vo -> {
            //添加组内的流程列表
            List<OaForms> oaForms = formsService.listByGroupId(vo.getGroupId(), formGroupVo.getKeywords());
            vo.setItems(oaForms);
        });
        return voList;
    }

    @Override
    @Transactional
    public void formGroupsSort(List<Long> groups) {
        for (int i = 0; i < groups.size(); i++) {
            OaFormGroups oaFormGroups = eruptDao.findById(OaFormGroups.class, groups.get(i));
            if (null != oaFormGroups) {
                oaFormGroups.setSort(++i);
                eruptDao.merge(oaFormGroups);
            }
        }
    }

    @Override
    @Transactional
    public void updateFormGroupName(Long id, String name) {
        eruptDao.findById(OaFormGroups.class, id).setGroupName(name);
    }

    @Override
    @Transactional
    public void createFormGroup(String name) {
        eruptDao.persist(OaFormGroups.builder().sort(1).groupName(name).updated(new Date()).build());
    }

    @Override
    @Transactional
    public void deleteFormGroup(Long id) {
        eruptDao.delete(OaFormGroups.builder().groupId(id).build());
    }

    @Override
    public List<OaFormGroups> getFormGroupList() {
        List<OaFormGroups> list = eruptDao.lambdaQuery(OaFormGroups.class).orderBy(OaFormGroups::getSort).list();
        //结尾添加两个固定分组
//        list.add(OaFormGroups.builder().groupId(0L).groupName("其他").sort(999).updated(new Date()).build());
        list.add(OaFormGroups.builder().groupId(-1L).groupName("已停用").sort(9999).updated(new Date()).build());
        return list;
    }
}
