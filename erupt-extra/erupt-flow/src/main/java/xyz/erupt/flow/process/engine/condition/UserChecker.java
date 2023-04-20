package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;

@Component
public class UserChecker implements ConditionChecker {

    /**
     * 用户检测有2种模式，发起人，或者人员选择字段
     * @param form
     * @param condition
     * @return
     */
    @Override
    public boolean check(JSONObject form, OaProcessNodeCondition condition) {
        return false;
    }
}
