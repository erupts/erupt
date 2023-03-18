package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.flow.bean.entity.OaFormGroups;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.mapper.OaFormGroupsMapper;
import xyz.erupt.flow.service.FormGroupService;
import xyz.erupt.flow.service.FormsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : willian fu
 * @date : 2022/7/4
 */
@Slf4j
@Service
public class FormGroupServiceImpl extends ServiceImpl<OaFormGroupsMapper, OaFormGroups> implements FormGroupService {

    @Autowired
    private FormsService formsService;

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
    @Transactional(rollbackFor = Exception.class)
    public void formGroupsSort(List<Long> groups) {
        List<OaFormGroups> updateList = new ArrayList<>();
        for (int i1 = 0; i1 < groups.size(); i1++) {
            updateList.add(
                OaFormGroups.builder()
                    .groupId(groups.get(i1))
                    .sort(i1)
                    .build()
            );
        }
        super.updateBatchById(updateList);
        return;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFormGroupName(Long id, String name) {
        super.updateById(OaFormGroups.builder().groupId(id).groupName(name).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFormGroup(String name) {
        super.save(OaFormGroups.builder().sort(1).groupName(name).updated(new Date()).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormGroup(Long id) {
        super.removeById(id);
    }

    @Override
    public List<OaFormGroups> getFormGroupList() {
        QueryWrapper<OaFormGroups> query = new QueryWrapper<OaFormGroups>()
                .orderByAsc("sort");
        List<OaFormGroups> list = super.list(query);
        //结尾添加两个固定分组
        list.add(OaFormGroups.builder()
                .groupId(0L)
                .groupName("其他")
                .sort(999)
                .updated(new Date())
                .build());
        list.add(OaFormGroups.builder()
                .groupId(-1L)
                .groupName("已停用")
                .sort(9999)
                .updated(new Date())
                .build());
        return list;
    }
}
