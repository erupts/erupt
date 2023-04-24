package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;
import xyz.erupt.flow.service.ProcessInstanceService;
import xyz.erupt.upms.service.EruptUserService;

import java.util.*;

@Component
public class UserChecker implements ConditionChecker {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private EruptUserService eruptUserService;

    /**
     * 用户检测有2种模式，发起人，或者人员选择字段
     * @param form
     * @param condition
     * @return
     */
    @Override
    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        String fieldId = condition.getId();
        Set<String> formValues = new HashSet<>();
        if("root".equals(fieldId)) {//取发起人进行对比
            if(execution!=null) {//已有实例，则取实例发起人
                OaProcessInstance instance = processInstanceService.getById(execution.getProcessInstId());
                formValues.add(instance.getCreator());
            }else {//否则取当前用户
                formValues.add(eruptUserService.getCurrentAccount());
            }
        }else {
            JSONArray jsonArray = form.getJSONArray(condition.getId());
            if(jsonArray!=null && jsonArray.size()>0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    formValues.add(jsonArray.getJSONObject(i).getString("id"));
                }
            }else {
                return false;//如果没有选值，则一定不符合要求
            }
        }
        String[] value = condition.getValue();//对照值
        if(value==null || value.length<=0) {
            throw new RuntimeException("条件没有对照值");
        }
        if(formValues==null) {//不能报错，因为可能是测试走流程
            throw new RuntimeException("分支条件不能为空");
        }
        // 判断 所选的用户 是否属于 参考值
        Iterator<String> iterator = formValues.iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();//取出所选用户
            boolean found = false;
            for (int i = 0; i < value.length; i++) {//循环匹配参考值
                if(next.equals(JSON.parseObject(value[i]).getString("id"))) {
                    found = true;
                    break;
                }
            }
            if(!found) {//如果没有匹配到，则条件不符合s
                return false;
            }
        }
        return true;
    }
}
