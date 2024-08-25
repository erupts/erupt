package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSONObject;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;

import java.text.ParseException;

/**
 * 检测条件
 */
public interface ConditionChecker {

    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) throws ParseException;
}
