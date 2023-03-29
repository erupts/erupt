package xyz.erupt.flow.process.engine;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeGroup;
import xyz.erupt.flow.service.ProcessActivityHistoryService;
import xyz.erupt.flow.service.ProcessExecutionService;

import java.util.List;

@Component
@Slf4j
public class ProcessHelper {

    @Lazy
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Lazy
    @Autowired
    private ProcessExecutionService processExecutionService;

    /**
     * 获取流程的上一步
     * @param execution
     * @return
     */
    public OaProcessActivity getLastActivity(OaProcessExecution execution) {
        //查询已经完成的所有活动
        List<OaProcessActivityHistory> histories =
                processActivityHistoryService.listByProcInstId(execution.getProcessInstId(), false);
        if(CollectionUtils.isEmpty(histories)) {
            if(execution.getParentId()!=null) {
                execution = processExecutionService.getById(execution.getParentId());
                return this.getLastActivity(execution);
            }
            return null;
        }
        //获得最近完成的活动
        OaProcessActivity activity = new OaProcessActivity();
        BeanUtils.copyProperties(histories.get(0), activity);
        return activity;
    }

    /**
     * 根据条件选择一个分支继续
     * @param formContent
     * @param nodes
     * @return
     */
    public OaProcessNode switchNode(JSONObject formContent, List<OaProcessNode> nodes) {
        //按照顺序判断是否满足条件
        for (OaProcessNode node : nodes) {
            try {
                if(checkForGroups(formContent, node.getProps().getGroups(), node.getProps().getGroupsType())) {
                    return node;
                }
            }catch (Exception e) {
                log.debug("判断条件出错：" + e.getMessage());
                break;
            }
        }
        //如果都不满足，默认走第一条
        return nodes.get(0);
    }

    /**
     * 判断条件组
     * @param groups
     * @param groupsType
     * @return
     */
    private boolean checkForGroups(JSONObject form, List<OaProcessNodeGroup> groups, String groupsType) {
        if("OR".equals(groupsType)) {
            for (OaProcessNodeGroup group : groups) {
                if(checkForConditions(form, group.getConditions(), group.getGroupType())) {
                    return true;//任何一个条件满足即可
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeGroup group : groups) {
                if(!checkForConditions(form, group.getConditions(), group.getGroupType())) {
                    return false;//任何一个不满足就返回false
                }
            }
            return true;
        }
    }

    private boolean checkForConditions(JSONObject form, List<OaProcessNodeCondition> conditions, String groupType) {
        if("OR".equals(groupType)) {//任何一个条件满足即可
            for (OaProcessNodeCondition condition : conditions) {
                if(checkForCondition(form, condition)) {
                    return true;
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeCondition condition : conditions) {
                if(!checkForCondition(form, condition)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkForCondition(JSONObject form, OaProcessNodeCondition condition) {
        String[] value = condition.getValue();//对照值
        if(value==null || value.length<=0) {
            throw new RuntimeException("条件没有对照值");
        }
        if("Number".equals(condition.getValueType())) {//数值类型
            Double formValue = form.getDouble(condition.getId());//表单值
            if(formValue==null) {//不能报错，因为可能是测试走流程
                throw new RuntimeException("分支条件不能为空");
            }
            if("=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))==0;
            }else if(">".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))>0;
            }else if("<".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))<0;
            }else if(">=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))>=0;
            }else if("<=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))<=0;
            }else if("IN".equals(condition.getCompare())) {//等于任意一个
                for (String s : value) {
                    if(formValue.compareTo(Double.valueOf(s))==0) {
                        return true;
                    }
                }
                return false;
            }else {
                if(value==null || value.length!=2) {
                    throw new RuntimeException("必须有2个对照值");
                }
                if("B".equals(condition.getCompare())) {//x < 值 < x，左右都是开区间
                    return formValue.compareTo(Double.valueOf(value[0]))>0 && formValue.compareTo(Double.valueOf(value[1]))<0;
                }else if("'AB'".equals(condition.getCompare())) {//x ≤ 值 < x，左闭右开
                    return formValue.compareTo(Double.valueOf(value[0]))>=0 && formValue.compareTo(Double.valueOf(value[1]))<0;
                }else if("'BA'".equals(condition.getCompare())) {//x < 值 ≤ x，左开右闭
                    return formValue.compareTo(Double.valueOf(value[0]))>0 && formValue.compareTo(Double.valueOf(value[1]))<=0;
                }else if("'ABA'".equals(condition.getCompare())) {//x ≤ 值 ≤ x，左右都是闭区间
                    return formValue.compareTo(Double.valueOf(value[0]))>=0 && formValue.compareTo(Double.valueOf(value[1]))<=0;
                }
            }
        }else {
            throw new RuntimeException("不支持此类条件判断"+condition.getValueType());
        }
        return false;
    }
}
