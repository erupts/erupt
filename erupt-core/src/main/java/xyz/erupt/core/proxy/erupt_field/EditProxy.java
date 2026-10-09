package xyz.erupt.core.proxy.erupt_field;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.AnnotationProxyPool;
import xyz.erupt.core.proxy.ProxyContext;
import xyz.erupt.core.proxy.erupt.FilterProxy;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.proxy.erupt_field.type.BoolTypeProxy;
import xyz.erupt.core.proxy.erupt_field.type.DateTypeProxy;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.TypeUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2022/2/6 10:13
 */
public class EditProxy extends AnnotationProxy<Edit, EruptField> {

    // One BoolType proxy per Edit; see boolType() below
    private BoolType boolTypeProxy;

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, Edit::type)) {
            if (EditType.AUTO == this.rawAnnotation.type()) {
                Field field = ProxyContext.get().getField();
                Class<?> fieldType = field.getType();
                String returnType = fieldType.getSimpleName();
                if (fieldType.isEnum()) {
                    return EditType.CHOICE; // options come from the enum constants, see EruptUtil.getChoiceList
                } else if (fieldType.isAnnotationPresent(Erupt.class)) {
                    // many-to-one: the target decides between a tree and a table picker
                    return AnnotationConst.EMPTY_STR.equals(fieldType.getAnnotation(Erupt.class).tree().pid()) ? EditType.REFERENCE_TABLE : EditType.REFERENCE_TREE;
                } else if (Collection.class.isAssignableFrom(fieldType)) {
                    Class<?> element = ReflectUtil.fieldGenericClass(field);
                    if (null != element && element.isEnum()) {
                        return EditType.MULTI_CHOICE;
                    } else if (null != element && element.isAnnotationPresent(Erupt.class)) {
                        // core has no JPA dependency, so the relation annotation is recognised by name
                        return ReflectUtil.hasAnnotationNamed(field, "ManyToMany") ? EditType.TAB_TABLE_REFER : EditType.TAB_TABLE_ADD;
                    }
                    return EditType.INPUT;
                } else if (Map.class.isAssignableFrom(fieldType)) {
                    return EditType.KEY_VALUE;
                } else if (boolean.class.getSimpleName().equalsIgnoreCase(returnType)) {
                    return EditType.BOOLEAN;
                } else if (TypeUtil.isNumberType(returnType)) {
                    return EditType.NUMBER;
                } else if (EruptUtil.isDateField(returnType)) {
                    return EditType.DATE;
                } else if (ArrayUtils.contains(AnnotationProcess.getEditTypeMapping(EditType.TEXTAREA).nameInfer(), returnType)) {
                    return EditType.TEXTAREA; // inferred by field name
                } else {
                    return EditType.INPUT;
                }
            }
            return this.rawAnnotation.type();
        } else if (super.matchMethod(invocation, Edit::filter)) {
            return FilterProxy.proxy(this.rawAnnotation.filter(), this);
        } else if (super.matchMethod(invocation, Edit::readonly)) {
            return AnnotationProxyPool.getOrPut(this.rawAnnotation.readonly(), readonly -> new ReadonlyProxy().newProxy(readonly, this));
        } else if (super.matchMethod(invocation, Edit::title)) {
            return ProxyContext.translate(this.rawAnnotation.title());
        } else if (super.matchMethod(invocation, Edit::desc)) {
            return ProxyContext.translate(this.rawAnnotation.desc());
        } else if (super.matchMethod(invocation, Edit::boolType)) {
            // Not pooled: the pool keys on BoolType equality, so every field with a default @BoolType would
            // share one proxy and one parent Edit, while BoolTypeProxy resolves AUTO from this Edit's notNull
            if (null == this.boolTypeProxy) {
                this.boolTypeProxy = new BoolTypeProxy().newProxy(this.rawAnnotation.boolType(), this);
            }
            return this.boolTypeProxy;
        } else if (super.matchMethod(invocation, Edit::dateType)) {
            return AnnotationProxyPool.getOrPut(this.rawAnnotation.dateType(), dateType -> new DateTypeProxy().newProxy(dateType, this));
        } else if (super.matchMethod(invocation, Edit::search)) {
            return AnnotationProxyPool.getOrPut(this.rawAnnotation.search(), search -> new SearchProxy().newProxy(search, this));
        }
        return this.invoke(invocation);
    }

}
