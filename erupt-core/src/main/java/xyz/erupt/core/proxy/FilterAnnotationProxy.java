package xyz.erupt.core.proxy;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2022/2/6 19:38
 */
public class FilterAnnotationProxy extends AnnotationProxy<Filter> {



    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (AnnotationConst.VALUE.equals(invocation.getMethod().getName())) {
            String condition = this.rawAnnotation.value();
            if (!this.rawAnnotation.conditionHandler().isInterface()) {
                FilterHandler ch = EruptSpringUtil.getBean(this.rawAnnotation.conditionHandler());
                condition = ch.filter(condition, this.rawAnnotation.params());
            }
            return condition;
        }
        return this.invoke(invocation);
    }

}
