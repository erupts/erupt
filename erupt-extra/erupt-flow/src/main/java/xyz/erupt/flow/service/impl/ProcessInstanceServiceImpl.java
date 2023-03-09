package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessInstanceMapper;
import xyz.erupt.flow.service.*;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ProcessInstanceServiceImpl extends ServiceImpl<OaProcessInstanceMapper, OaProcessInstance>
        implements ProcessInstanceService, DataProxy<OaProcessInstance> {

    @Autowired
    private EruptUserService eruptUserService;
    @Autowired
    private ProcessInstanceHistoryService processInstanceHistoryService;
    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Autowired
    private TaskService taskService;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        //删除任务
        taskService.removeByProcessInstId(id);
        //删除节点
        processActivityService.removeByProcessInstId(id);
        //删除execution
        processExecutionService.removeByProcessInstId(id);
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

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        list.forEach(m -> {
            //拼接详情链接
            m.put("detailLink", FlowConstant.SERVER_NAME+"/index.html#/detail/"+ m.get("id") +"/view");
        });
    }
}
