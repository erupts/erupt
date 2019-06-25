package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateEnum;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by liyuepeng on 11/1/18.
 */
public class EruptUtil {

    //数据项与erupt注解中描述不相符时使用
    private static final String NOT_ERUPT_REF = "@NOT_REF@";

    private static final EditType[] OBJECT_EDIT_TYPES = {EditType.COMBINE, EditType.REFERENCE_TREE, EditType.REFERENCE_TABLE, EditType.TAB};

    //请求地址权限校验
    public static String handleNoRightVariable(String pathVariable) {
        if (pathVariable.startsWith(RestPath.NO_RIGHT_SYMBOL)) {
            return pathVariable.substring(2);
        } else {
            throw new RuntimeException("数据参数异常");
        }
    }

    //清空一对多关系数据，防止循环引用导致内存溢出  TODO 使用 generateEruptDataMap方法代替
    public static void rinseEruptObj(Object eruptObj) {
        if (null != eruptObj) {
            Erupt erupt = eruptObj.getClass().getAnnotation(Erupt.class);
            try {
                for (Field field : eruptObj.getClass().getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && null == field.getAnnotation(EruptField.class)) {
                        field.setAccessible(true);
                        field.set(eruptObj, null);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            findClassAllEruptFields(eruptObj.getClass(), field -> {
                field.setAccessible(true);
                EruptField eruptField = field.getAnnotation(EruptField.class);
                if (field.getName().equalsIgnoreCase(erupt.primaryKeyCol())) {
                    return;
                }
                try {
                    if (null == eruptField || StringUtils.isBlank(eruptField.edit().title())) {
                        field.set(eruptObj, null);
                    } else {
                        if (eruptField.edit().type() == EditType.TAB) {
                            field.set(eruptObj, null);
                        } else if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                            try {
                                //仅保留id与label
                                ReferenceTreeType referenceTreeType = eruptField.edit().referenceTreeType();
                                Object obj = field.get(eruptObj);
                                if (null != obj) {
                                    Object newObj = obj.getClass().newInstance();
                                    ReflectUtil.findClassField(newObj.getClass(), referenceTreeType.id()).set(newObj,
                                            ReflectUtil.findClassField(obj.getClass(), referenceTreeType.id()).get(obj));
                                    ReflectUtil.findClassField(newObj.getClass(), referenceTreeType.label()).set(newObj,
                                            ReflectUtil.findClassField(obj.getClass(), referenceTreeType.label()).get(obj));
                                    field.set(eruptObj, newObj);
                                }
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            }
                        } else if (eruptField.edit().type() == EditType.REFERENCE_TABLE) {
                            rinseEruptObj(field.get(eruptObj));
                        } else if (eruptField.edit().type() == EditType.COMBINE) {
                            rinseEruptObj(field.get(eruptObj));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    //将object中erupt标识的字段抽取出来放到map中
    public static Map<String, Object> generateEruptDataMap(Object obj) {
        final Map<String, Object> map = new HashMap<>();
        findClassAllEruptFields(obj.getClass(), field -> {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                if (null != fieldValue) {
                    EruptField eruptField = field.getAnnotation(EruptField.class);
                    switch (eruptField.edit().type()) {
                        case REFERENCE_TREE:
                        case REFERENCE_TABLE:
                            String id = null;
                            String label = null;
                            if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                                ReferenceTreeType referenceTreeType = eruptField.edit().referenceTreeType();
                                id = referenceTreeType.id();
                                label = referenceTreeType.label();
                            } else if (eruptField.edit().type() == EditType.REFERENCE_TABLE) {
                                ReferenceTableType referenceTableType = eruptField.edit().referenceTableType();
                                id = referenceTableType.id();
                                label = referenceTableType.label();
                            }
                            Map<String, Object> referMap = new HashMap<>();
                            referMap.put(id, ReflectUtil.findClassField(field.get(obj).getClass(), id).get(fieldValue));
                            referMap.put(label, ReflectUtil.findClassField(field.get(obj).getClass(), label).get(fieldValue));
                            map.put(field.getName(), referMap);
                            break;
                        case COMBINE:
                            Map<String, Object> subMap = new HashMap<>();
                            Object refObj = field.get(obj);
                            findClassAllEruptFields(refObj.getClass(), f -> {
                                try {
                                    f.setAccessible(true);
                                    subMap.put(f.getName(), f.get(refObj));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }, OBJECT_EDIT_TYPES);
                            map.put(field.getName(), subMap);
                            break;
                        case TAB:
                            break;
                        default:
                            map.put(field.getName(), field.get(obj));
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return map;
    }

    //递归查找类字段
    private static void findClassAllEruptFields(Class clazz, Consumer<Field> consumer, EditType... excludeEditType) {
        if (null != clazz) {
            for (Field field : clazz.getDeclaredFields()) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                if (null != eruptField) {
                    boolean allow = true;
                    for (EditType type : excludeEditType) {
                        if (eruptField.edit().type() == type) {
                            allow = false;
                            break;
                        }
                    }
                    if (allow) {
                        consumer.accept(field);
                    }
                }
            }
            Class superClass = clazz.getSuperclass();
            if (null != superClass && !superClass.getSimpleName().equals("Object")) {
                findClassAllEruptFields(superClass, consumer);
            }
        }
    }

    //请求参数转换
    public static Object jsonElementToObject(EruptFieldModel eruptFieldModel, JsonElement jsonElement) {
        if (EruptFieldModel.NUMBER_TYPE.equalsIgnoreCase(eruptFieldModel.getFieldReturnName())) {
            try {
                return jsonElement.getAsInt();
            } catch (Exception e) {
                return 0;
            }
        }
        switch (eruptFieldModel.getEruptField().edit().type()) {
            case SLIDER:
                return jsonElement.getAsInt();
            case BOOLEAN:
                return jsonElement.getAsBoolean();
            case DATE:
                DateEnum dateEnum = eruptFieldModel.getEruptField().edit().dateType().type();
                if (dateEnum == DateEnum.DATE || dateEnum == DateEnum.DATE_TIME) {
                    return DateUtil.getDate(jsonElement.getAsString());
                } else {
                    return jsonElement.getAsString();
                }
            case REFERENCE_TREE:
                //TODO 类型转换太频繁,以后优化
                String id = eruptFieldModel.getEruptField().edit().referenceTreeType().id();
                EruptFieldModel efm = CoreService.getErupt(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(id);
                return TypeUtil.typeStrConvertObject(jsonElement.getAsJsonObject().get(id).getAsString(), efm.getField().getType().getSimpleName());
            default:
                return jsonElement.getAsString();
        }
    }


    public static BoolAndReason validateEruptNotNull(EruptModel eruptModel, JsonObject data) {
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            if (field.getEruptField().edit().notNull()) {
                if (!data.has(field.getFieldName())) {
                    return new BoolAndReason(false, field.getEruptField().edit().title() + "必填");
                }
            }
        }
        return new BoolAndReason(true, "");
    }


    public Map<String, Object> eruptDataToViewData(Object data) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (Field field : data.getClass().getDeclaredFields()) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                Object fieldData = field.get(data);
                if (null != eruptField) {
                    switch (eruptField.edit().type()) {
                        case INPUT:
                            result.put(field.getName(), fieldData);
                            break;
                        case CHOICE:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                for (VL vl : eruptField.edit().choiceType().vl()) {
                                    if ((vl.value() + "").equals(fieldData.toString())) {
                                        result.put(field.getName(), fieldData);
                                        break;
                                    }
                                }
                                //如果与VL注解无匹配项则注入该标识信息
                                if (StringUtils.isBlank(result.get(field.getName()).toString())) {
                                    result.put(field.getName(), NOT_ERUPT_REF);
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        case BOOLEAN:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                Boolean boolField = Boolean.valueOf(fieldData.toString());
                                if (boolField) {
                                    result.put(field.getName(), eruptField.edit().boolType().trueText());
                                } else {
                                    result.put(field.getName(), eruptField.edit().boolType().falseText());
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        case REFERENCE_TREE:
                            if (StringUtils.isNotBlank(fieldData.toString())) {
                                for (View view : eruptField.views()) {
                                    result.put(field.getName() + "_" + view.column(),
                                            fieldData.getClass().getDeclaredField(view.column()).get(fieldData));
                                }
                            } else {
                                result.put(field.getName(), NOT_ERUPT_REF);
                            }
                            break;
                        default:
                            result.put(field.getName(), field.get(data));
                            break;
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return result;
    }


}
