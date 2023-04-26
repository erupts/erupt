package xyz.erupt.flow.process.engine.condition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeCondition;
import xyz.erupt.flow.repository.EruptUserRepository;
import xyz.erupt.flow.service.ProcessInstanceService;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class UserChecker implements ConditionChecker {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private EruptUserService eruptUserService;
    @Autowired
    private EruptUserRepository eruptUserRepository;
    @Autowired
    private DeptChecker deptChecker;

    /**
     * 所选用户分为2种，发起人，或者人员选择字段
     * 比较方式分为三种：人员、部门、角色
     * @param form
     * @param condition
     * @return
     */
    @Override
    public boolean check(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        String fieldId = condition.getId();
        Set<String> formValues = new HashSet<>();
        //所选人员属于集合之一，选择的每个人都要命中才返回true
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
            log.error("条件没有对照值");
            return false;
        }
        if(formValues==null) {//不能报错，因为可能是测试走流程
            log.error("分支条件不能为空");
            return false;
        }
        // 根据三种不同的比较符进行判断
        if("user".equals(condition.getCompare())) {
            return compareForUser(formValues, value);
        }else if("dept".equals(condition.getCompare())) {
            return compareForDept(formValues, value);
        }else if("role".equals(condition.getCompare())) {
            return compareForRole(formValues, value);
        }
        log.error("比较符无法识别"+condition.getCompare());
        return false;
    }

    public boolean compareForUser(Set<String> formValues, String[] value) {
        if(formValues==null || formValues.size()<=0) {
            return false;
        }
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
            if(!found) {//如果没有匹配到，则条件不符合
                return false;
            }
        }
        return true;
    }

    public boolean compareForDept(Set<String> formValues, String[] value) {
        if(formValues==null || formValues.size()<=0) {
            return false;
        }
        List<EruptUser> users = eruptUserRepository.findByAccountIn(formValues);
        for (EruptUser user : users) {
            if(user.getEruptOrg()==null) {//任意用户没有部门则返回false
                return false;
            }
            Long deptId = user.getEruptOrg().getId();
            boolean found = false;
            for (int i = 0; i < value.length; i++) {//循环匹配参考值
                //部门id是long型的
                if(deptId.equals(JSON.parseObject(value[i]).getLongValue("id"))) {
                    found = true;
                    break;
                }else if(deptChecker.instanceOfDept(deptId, JSON.parseObject(value[i]).getLongValue("id"))) {
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

    public boolean compareForRole(Set<String> formValues, String[] value) {
        if(formValues==null || formValues.size()<=0) {
            return false;
        }
        Iterator<String> iterator = formValues.iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();//取出所选用户
            EruptUser account = eruptUserRepository.findByAccount(next);//查询用户信息
            boolean found = false;
            for (int i = 0; i < value.length; i++) {//循环匹配参考值
                if(account.getRoles()!=null) {
                    for (EruptRole role : account.getRoles()) {
                        if(role.getCode().equals(JSON.parseObject(value[i]).getString("id"))) {
                            found = true;//任意一个角色命中即可
                            break;
                        }
                    }
                }
            }
            if(!found) {//如果没有匹配到，则条件不符合s
                return false;
            }
        }
        return true;
    }
}
