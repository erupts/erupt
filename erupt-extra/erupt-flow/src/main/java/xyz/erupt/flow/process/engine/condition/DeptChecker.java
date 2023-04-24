package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;

@Component
public class DeptChecker implements ConditionChecker {
    @Override
    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        return false;
    }
}
