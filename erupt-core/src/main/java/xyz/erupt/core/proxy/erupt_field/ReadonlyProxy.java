package xyz.erupt.core.proxy.erupt_field;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * readonly proxy
 *
 * @author YuePeng
 * date 2022/2/6 20:36
 */
public class ReadonlyProxy extends AnnotationProxy<Readonly, Edit> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        Readonly readonly = this.rawAnnotation;
        if (!readonly.exprHandler().isInterface()) {
            Readonly.ReadonlyHandler readonlyHandler = EruptSpringUtil.getBean(readonly.exprHandler());
            switch (invocation.getMethod().getName()) {
                case "add":
                    return readonlyHandler.add(readonly.add(), readonly.params());
                case "edit":
                    return readonlyHandler.edit(readonly.edit(), readonly.params());
            }
        }
        return this.invoke(invocation);
    }

}
