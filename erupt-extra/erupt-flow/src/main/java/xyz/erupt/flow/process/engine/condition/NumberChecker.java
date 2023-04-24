package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;

@Component
@Slf4j
public class NumberChecker implements ConditionChecker {

    @Override
    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        Double formValue = form.getDouble(condition.getId());//表单值

        String[] value = condition.getValue();//对照值
        if(value==null || value.length<=0) {
            log.error("条件没有对照值");
            return false;
        }
        if(formValue==null) {//不能报错，因为可能是测试走流程
            log.error("分支条件不能为空");
            return false;
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
        return false;
    }
}
