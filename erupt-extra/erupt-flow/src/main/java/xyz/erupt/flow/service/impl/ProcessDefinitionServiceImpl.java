package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessDefinitionMapper;
import xyz.erupt.flow.service.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessDefinitionServiceImpl extends ServiceImpl<OaProcessDefinitionMapper, OaProcessDefinition>
        implements ProcessDefinitionService, DataProxy<OaProcessDefinition> {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private FormGroupService formGroupService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Autowired
    private ProcessInstanceHistoryService processInstanceHistoryService;

    @Override
    public void beforeDelete(OaProcessDefinition oaProcessDefinition) {
        long count = processInstanceService.countByProcessDefinitionId(oaProcessDefinition.getId());
        if(count>0) {
            throw new EruptApiErrorTip("请先删除业务数据之后再删除流程");
        }
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        if(list==null || list.size()<=0) {
            return;
        }
        Set<Long> groupIds = new HashSet<>();
        for (Map<String, Object> map : list) {
            if(map.get("groupId")!=null) {
                Long groupId = Long.valueOf(map.get("groupId").toString());
                groupIds.add(groupId);
            }
        }
        if(groupIds.size()>0) {
            List<OaFormGroups> oaFormGroups = formGroupService.listByIds(groupIds);
            if(oaFormGroups!=null) {
                Map<Long, OaFormGroups> groupsMap = oaFormGroups.stream()
                        .collect(Collectors.toMap(OaFormGroups::getGroupId, e -> e, (key1, key2) -> key1));
                list.forEach(map -> {
                    OaFormGroups group = groupsMap.get(Long.valueOf(map.get("groupId").toString()));
                    if(group!=null) {
                        map.put("groupName", group.getGroupName());
                    }
                });
            }
        }
    }

    @Override
    public void updateStopByFormId(Long formId, boolean isStop) {
        QueryWrapper<OaProcessDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessDefinition::getFormId, formId);
        //批量修改禁用状态
        super.update(
                OaProcessDefinition.builder()
                        .isStop(isStop)
                        .build()
                , queryWrapper);
    }

    @Override
    public void updateByFormId(OaProcessDefinition update, Long formId) {
        QueryWrapper<OaProcessDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessDefinition::getFormId, formId);
        super.update(update , queryWrapper);
    }

    @Override
    public void removeByFormId(Long formId) {
        //判断是否有流程实例存在，这里先不考虑历史数据
        long count = processInstanceService.countByFormId(formId);
        if(count>0) {
            throw new EruptApiErrorTip("请先删除业务数据之后再删除流程");
        }
        //删除
        QueryWrapper<OaProcessDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessDefinition::getFormId, formId);
        super.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaProcessInstance startById(String defId, String content) {
        //根据formId查询出最新版
        OaProcessDefinition processDef = this.getById(defId);
        if(processDef==null) {
            throw new EruptApiErrorTip("流程"+defId+"不存在");
        }
        if(processDef.getIsStop()) {
            throw new EruptApiErrorTip("流程可能已经过期，请刷新后重试");
        }
        //创建流程实例
        OaProcessInstance processInstance = processInstanceService.newProcessInstance(processDef, content);
        return processInstance;
    }

    @Override
    public OaProcessDefinition getLastVersionByFromId(Long formId) {
        QueryWrapper<OaProcessDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessDefinition::getFormId, formId);
        queryWrapper.lambda().orderByDesc(OaProcessDefinition::getVersion);
        PageHelper.startPage(1, 1);//分页查询
        List<OaProcessDefinition> list = super.list(queryWrapper);
        if(list==null || list.size()<=0) {
            return null;
        }else {
            return list.get(0);
        }
    }

    @Override
    public List<OaProcessActivityHistory> preview(String formDefId, JSONObject formContent) {
        OaProcessDefinition procDef = this.getById(formDefId);
        List<OaProcessActivityHistory> activities = new ArrayList<>();
        Map<String, OaProcessActivityHistory> map;
        this.preview(JSON.parseObject(procDef.getProcess(), OaProcessNode.class), formContent, activities, new HashMap<>());
        return activities;
    }

    @Override
    public List<OaProcessActivityHistory> preview(Long instId) {
        OaProcessInstanceHistory inst = processInstanceHistoryService.getById(instId);
        OaProcessDefinition procDef = this.getById(inst.getProcessDefId());
        List<OaProcessActivityHistory> activities = new ArrayList<>();
        //查询出已完成的活动
        List<OaProcessActivityHistory> histories = processActivityHistoryService.listByProcInstId(instId);
        //形成一个map
        Map<String, List<OaProcessActivityHistory>> map =
                histories.stream().collect(Collectors.groupingBy(OaProcessActivityHistory::getActivityKey));
        JSONObject formContent = JSON.parseObject(inst.getFormItems());
        this.preview(JSON.parseObject(procDef.getProcess(), OaProcessNode.class), formContent, activities, map);
        return histories;
    }

    @Override
    public List<OaFormGroups> getFormGroups(OaFormGroups build) {
        List<OaFormGroups> voList = formGroupService.getFormGroupList();
        //循环添加组内的表单
        voList.forEach(vo -> {
            //添加组内的流程列表
            List<OaProcessDefinition> oaForms = this.listByGroupId(vo.getGroupId(), build.getKeywords());
            vo.setProcessDefs(oaForms);
        });
        return voList;
    }

    @Override
    public void deploy(OaForms forms) {
        //创建新版本
        OaProcessDefinition oaProcessDefinition = new OaProcessDefinition();
        BeanUtils.copyProperties(forms, oaProcessDefinition);
        Date now = new Date();
        oaProcessDefinition.setCreated(now);
        OaProcessDefinition last = this.getLastVersionByFromId(forms.getFormId());
        if(last==null) {
            oaProcessDefinition.setVersion(1);
        }else {
            oaProcessDefinition.setVersion(last.getVersion()+1);
        }
        //表单id拼接版本，作为流程定义的id
        oaProcessDefinition.setId(forms.getFormId()+"_"+oaProcessDefinition.getVersion());
        //旧版本全部停用
        this.updateStopByFormId(forms.getFormId(), true);
        this.save(oaProcessDefinition);
    }

    private List<OaProcessDefinition> listByGroupId(Long groupId, String keywords) {
        LambdaQueryWrapper<OaProcessDefinition> wrapper = new LambdaQueryWrapper<OaProcessDefinition>()
                .eq(OaProcessDefinition::getGroupId, groupId)
                .eq(OaProcessDefinition::getIsStop, false)//只查询可用的
                .orderByAsc(OaProcessDefinition::getSort);
        if(StringUtils.isNotEmpty(keywords)) {
            wrapper.like(OaProcessDefinition::getFormName, keywords);
        }
        return this.list(wrapper);
    }

    private void preview(OaProcessNode node, JSONObject formContent, List<OaProcessActivityHistory> activities, Map<String, List<OaProcessActivityHistory>> map) {
        if(node == null || node.getId() == null) {//如果当前节点为空
            return;
        }else if(FlowConstant.NODE_TYPE_ROOT.equals(node.getType())
                || FlowConstant.NODE_TYPE_APPROVAL.equals(node.getType())
        ) {//如果是用户任务
            //优先从map中获取，获取不到才读取流程图
            if(map.get(node.getId())!=null) {
                activities.addAll(map.get(node.getId()));
            }else {
                activities.add(OaProcessActivityHistory
                        .builder()
                        .activityKey(node.getId())
                        .activityName(node.getName())
                        .description(node.getDesc())
                        .build()
                );
            }
        }else if(FlowConstant.NODE_TYPE_CONDITIONS.equals(node.getType())) {//如果是互斥分支
            //根据条件选择一个分支
            OaProcessNode nextNode = processActivityService.switchNode(formContent, node.getBranchs());
            //先追加该分支
            this.preview(nextNode, formContent, activities, map);
        }else if(FlowConstant.NODE_TYPE_CONCURRENTS.equals(node.getType())) {//如果是并行分支
            //循环为该虚拟分支增加节点
            //TODO 这里有点问题，应该把并行的分支同步追加，但是因为前端不好展示，这里先按顺序追加
            for (int i = 0; i < node.getBranchs().size(); i++) {
                this.preview(node.getBranchs().get(i), formContent, activities, map);
            }
        }
        //然后追加子分支
        this.preview(node.getChildren(), formContent, activities, map);
    }
}
