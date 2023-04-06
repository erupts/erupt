package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskHistory;
import xyz.erupt.flow.bean.entity.OaTaskUserLink;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaTaskHistoryMapper;
import xyz.erupt.flow.service.ProcessDefinitionService;
import xyz.erupt.flow.service.TaskHistoryService;
import xyz.erupt.flow.service.TaskUserLinkService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskHistoryServiceImpl extends ServiceImpl<OaTaskHistoryMapper, OaTaskHistory>
        implements TaskHistoryService, DataProxy<OaTaskHistory> {

    @Autowired
    @Lazy
    private ProcessDefinitionService processDefinitionService;
    @Autowired
    private TaskUserLinkService taskUserLinkService;

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        Map<String, OaProcessDefinition> definitionMap = new HashMap<>();

        for (Map<String, Object> map : list) {
            OaProcessDefinition def = definitionMap.get(map.get("processDefId"));
            if(def==null) {
                def = processDefinitionService.getById((String) map.get("processDefId"));
                definitionMap.put((String) map.get("processDefId")
                        , def==null?new OaProcessDefinition():def);
            }
            map.put("formName", def.getFormName());//填充流程名

            //如果是未完成，且激活的任务，查询一下候选人
            if(!(Boolean)map.get("finished") && (Boolean) map.get("active")
                && StringUtils.isBlank((String)map.get("taskOwner"))
                && StringUtils.isBlank((String) map.get("assignee"))
            ) {
                List<OaTaskUserLink> links =
                        taskUserLinkService.listByTaskId((Long) map.get("id"));
                if(!CollectionUtils.isEmpty(links)) {
                    String str = null;

                    for (int i = 0; i < links.size(); i++) {
                        if(links.get(i).getUserLinkType().equals(FlowConstant.USER_LINK_ROLES)) {
                            str += "[角色]";
                        }else if(links.get(i).getUserLinkType().equals(FlowConstant.USER_LINK_USERS)){
                            str = "[用户]";
                        }else if(links.get(i).getUserLinkType().equals(FlowConstant.USER_LINK_CC)){
                            str = "[抄送]";
                        }
                        if(i>0) {
                            str += "," + links.get(i).getLinkName();
                        }else {
                            str += links.get(i).getLinkName();
                        }
                    }
                    map.put("links", str);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaTaskHistory copyAndSave(OaTask task) {
        OaTaskHistory oaTaskHistory = new OaTaskHistory();
        BeanUtils.copyProperties(task, oaTaskHistory);
        super.saveOrUpdate(oaTaskHistory);
        return oaTaskHistory;
    }

    @Override
    public List<OaTaskHistory> copyAndSave(Collection<OaTask> tasks) {
        List<OaTaskHistory> collect = tasks.stream().map(t -> {
            OaTaskHistory hist = new OaTaskHistory();
            BeanUtils.copyProperties(t, hist);
            return hist;
        }).collect(Collectors.toList());
        this.saveOrUpdateBatch(collect);
        return collect;
    }

    @Override
    public List<OaTaskHistory> listByActivityId(Long activityId) {
        return this.list(new LambdaQueryWrapper<OaTaskHistory>()
                .eq(OaTaskHistory::getActivityId, activityId)
                .orderByAsc(OaTaskHistory::getCompleteSort)
        );
    }
}
