package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Component
@Slf4j
public class DeptChecker implements ConditionChecker {
    @Override
    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        /** 获取选中的值 */
        Set<String> formValues = new HashSet<>();
        JSONArray jsonArray = form.getJSONArray(condition.getId());
        if(jsonArray!=null && jsonArray.size()>0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                formValues.add(jsonArray.getJSONObject(i).getString("id"));
            }
        }else {
            return false;//如果没有选值，则一定不符合要求
        }
        /** 获取参考值 */
        String[] value = condition.getValue();//对照值

        if(value==null || value.length<=0) {
            log.error("条件没有对照值");
            return false;
        }
        if(formValues==null) {//不能报错，因为可能是测试走流程
            log.error("分支条件不能为空");
            return false;
        }
        // 根据不同的比较符进行判断
        if("dept".equals(condition.getCompare())) {
            return compareForDept(formValues, value);
        }
        log.error("比较符无法识别"+condition.getCompare());
        return false;
    }

    public boolean compareForDept(Set<String> formValues, String[] value) {
        if(formValues==null || formValues.size()<=0) {
            return false;
        }
        Iterator<String> iterator = formValues.iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();//取出所选用户
            boolean found = false;
            for (int i = 0; i < value.length; i++) {//循环匹配参考值
                //部门id是long型的
                if(next.equals(JSON.parseObject(value[i]).getString("id"))) {
                    found = true;
                    break;
                }
            }
            if(!found) {//如果没有匹配到，则条件不符合
                return false;
            }
        }
        return true;
    }
}
