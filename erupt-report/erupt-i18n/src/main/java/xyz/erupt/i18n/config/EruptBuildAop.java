package xyz.erupt.i18n.config;

import com.google.gson.Gson;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author YuePeng
 * date 2021/9/12 23:55
 */
public class EruptBuildAop {

    @Pointcut("execution(public * xyz.erupt.core.controller.EruptBuildController.getEruptBuild(..))")
    public void cut() {
    }

    @AfterReturning("cut()")
    public void doAfterReturning(Object obj) {
        System.out.println(new Gson().toJson(obj));
    }

}
