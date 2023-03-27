package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessInstanceMapper;
import xyz.erupt.flow.service.*;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessInstanceServiceImpl extends ServiceImpl<OaProcessInstanceMapper, OaProcessInstance>
        implements ProcessInstanceService, DataProxy<OaProcessInstance> {

    @Autowired
    private EruptUserService eruptUserService;
    @Autowired
    private ProcessInstanceHistoryService processInstanceHistoryService;
    @Lazy
    @Autowired
    private ProcessActivityService processActivityService;
    @Lazy
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Lazy
    @Autowired
    private TaskService taskService;
    @Lazy
    @Autowired
    private TaskHistoryService taskHistoryService;
    @Autowired
    private TaskOperationService taskOperationService;
    @Autowired
    private TaskUserLinkService taskUserLinkService;
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Autowired
    @Lazy
    private ProcessDefinitionService processDefinitionService;

    /**
     * 启动新的流程实例（instance），会级联创建线程（execution），并创建第一个节点（activity）
     * @param processDef
     * @param content 表单内容
     * @return
     */
    @Override
    public OaProcessInstance newProcessInstance(OaProcessDefinition processDef, String content) {
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        Date now = new Date();
        OaProcessInstance build = OaProcessInstance.builder()
                .processDefId(processDef.getId())
                .formId(processDef.getFormId())
                .formName(processDef.getFormName())
                .businessKey(processDef.getId()+"_business_key")
                .businessTitle(currentEruptUser.getName() + "的《" + processDef.getFormName() + "》工单")
                .status(OaProcessInstance.RUNNING)
                .creator(currentEruptUser.getAccount())
                .creatorName(currentEruptUser.getName())
                .createDate(now)
                .formItems(content)
                .build();
        //保存数据
        super.save(build);
        //保存历史数据
        processInstanceHistoryService.copyAndSave(build);

        //从根节点开始新建线程
        OaProcessNode rootNode = JSON.parseObject(processDef.getProcess(), OaProcessNode.class);
        processExecutionService.newExecution(processDef.getId(), build.getId(), JSON.parseObject(content), rootNode, null);
        return build;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long processInstId) {
        OaProcessInstanceHistory build = OaProcessInstanceHistory.builder()
                .build();
        build.setStatus(OaProcessInstance.FINISHED);
        build.setFinishDate(new Date());
        build.setId(processInstId);
        processInstanceHistoryService.updateById(build);//同步更新历史表
        this.removeById(processInstId);//删除运行时表
    }

    @Override
    public List<OaProcessInstanceHistory> getMineAbout(String keywords, int pageNum, int pageSize) {
        String account = eruptUserService.getCurrentAccount();
        //查询我处理过的所有任务（发起和审批的）
        List<OaTaskOperation> operations = taskOperationService.listByOperator(account);
        //查询抄送我的所有任务
        List<OaTaskUserLink> links = taskUserLinkService.listByUserIds(Arrays.asList(account));

        Set<Long> taskIds = operations.stream().map(e -> e.getTaskId()).collect(Collectors.toSet());
        Set<Long> linkTaskIds = links.stream().map(e -> e.getTaskId()).collect(Collectors.toSet());

        if(CollectionUtils.isEmpty(taskIds) && CollectionUtils.isEmpty(linkTaskIds)) {
            return new ArrayList<>(0);
        }
        Set<Long> allTaskIds = new HashSet<>();
        allTaskIds.addAll(taskIds);
        allTaskIds.addAll(linkTaskIds);
        List<OaTaskHistory> tasks = taskHistoryService.listByIds(allTaskIds);
        if(CollectionUtils.isEmpty(tasks)) {
            return new ArrayList<>();
        }
        //根据任务id查询流程实例
        LambdaQueryWrapper<OaProcessInstanceHistory> queryWrapper = new LambdaQueryWrapper<OaProcessInstanceHistory>()
                .in(OaProcessInstanceHistory::getId, tasks.stream().map(t -> t.getProcessInstId()).collect(Collectors.toSet()))
                .orderByDesc(OaProcessInstanceHistory::getCreateDate);
        PageHelper.startPage(pageNum, pageSize);//分页
        List<OaProcessInstanceHistory> list = processInstanceHistoryService.list(queryWrapper);
        //先把所有任务分类
        Map<Long, OaTaskHistory> taskMap
                = tasks.stream().collect(Collectors.toMap(OaTaskHistory::getProcessInstId, e -> {
                    //最优先发起
                    if(FlowConstant.NODE_TYPE_ROOT.equals(e.getTaskType())) {
                        e.setTag("发起");
                        return e;
                    }else if(FlowConstant.NODE_TYPE_APPROVAL.equals(e.getTaskType())) {
                        e.setTag("审批");
                        return e;
                    }else if(linkTaskIds.contains(e.getId())) {
                            e.setTag("抄送");
                            return e;
                    }
                    e.setTag("审批");
                    return e;
                }, (v1, v2) -> {
                    //最优先发起
                    if("发起".equals(v1.getTag())) {
                        return v1;
                    }else if("发起".equals(v2.getTag())) {
                        return v2;
                    }
                    //其次是审批
                    if("审批".equals(v1.getTag())) {
                        return v1;
                    }else if("审批".equals(v2.getTag())) {
                        return v2;
                    }
                    //其次是审批
                    if("抄送".equals(v1.getTag())) {
                        return v1;
                    }else if("抄送".equals(v2.getTag())) {
                        return v2;
                    }
                    return v1;
                }
        ));
        //分类打标记
        taskMap.forEach((instId, t) -> {
            for (OaProcessInstanceHistory inst : list) {
                if(inst.getId().equals(instId)) {
                    inst.setTag(t.getTag());
                }
            }
        });
        return list;
    }

    /**
     * 停止流程实例
     * @param instId
     * @param remarks
     */
    @Override
    public void stop(Long instId, String remarks) {
        OaProcessInstanceHistory build = OaProcessInstanceHistory.builder()
                .status(OaProcessInstance.SHUTDOWN)
                .finishDate(new Date())
                .reason(remarks)
                .id(instId)
                .build();
        processInstanceHistoryService.updateById(build);//同步更新历史表
        this.removeById(instId);//删除运行时表
    }

    /**
     * 跳转到指定活动
     * @param execution
     * @param nodeId
     */
    @Override
    public void jumpTo(OaProcessExecution execution, String nodeId) {
        //跳转之前，要先确定是本线程跳转还是跨线程跳转
        //本线程内的跳转，不需要停止全部
        //processExecutionService.stopByInstId(execution.getProcessInstId(), "节点跳转");
        processActivityService.stopByExecutionId(execution.getProcessInstId(), "节点跳转");
        taskService.stopByExecutionId(execution.getProcessInstId(), "节点跳转");
        //然后新启动一个线程，从跳转部分开始
        OaProcessNode node =
                processDefinitionService.readNode(execution.getProcessDefId(), nodeId);
        OaProcessInstance inst = this.getById(execution.getProcessInstId());
        JSONObject formContent = JSON.parseObject(inst.getFormItems());
        //当前线程下，继续进行
        processActivityService.newActivities(execution, formContent, node);
    }

    /**
     * 获取流程的上一步
     * @param execution
     * @return
     */
    @Override
    public OaProcessActivity getLastActivity(OaProcessExecution execution) {
        //查询已经完成的所有活动
        List<OaProcessActivityHistory> histories =
                processActivityHistoryService.listByProcInstId(execution.getProcessInstId(), false);
        if(CollectionUtils.isEmpty(histories)) {
            if(execution.getParentId()!=null) {
                return this.getLastActivity(processExecutionService.getById(execution.getParentId()));
            }
            return null;
        }
        //获得最近完成的活动
        OaProcessActivity activity = new OaProcessActivity();
        BeanUtils.copyProperties(histories.get(0), activity);
        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        //删除任务
        taskService.removeByProcessInstId((Long) id);
        //删除活动
        processActivityService.removeByProcessInstId((Long) id);
        //删除execution
        processExecutionService.removeByProcessInstId((Long) id);
        //删除流程实例
        return super.removeById(id);
    }

    @Override
    public long countByProcessDefinitionId(String procDefId) {
        QueryWrapper<OaProcessInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessInstance::getProcessDefId, procDefId);
        return super.count(queryWrapper);
    }

    @Override
    public long countByFormId(Long formId) {
        QueryWrapper<OaProcessInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessInstance::getFormId, formId);
        return super.count(queryWrapper);
    }
}
