package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptFieldAndValue;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
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

    //清空一对多关系数据，防止循环引用导致内存溢出  TODO 代码还需要优化  使用 generateEruptDataMap方法代替
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
            findClassAllEruptFields(eruptObj, field -> {
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
        List<EruptFieldAndValue> eruptFieldAndValues = new ArrayList<>();
        try {
            findClassAllEruptFields(obj, eruptFieldAndValues);
            for (EruptFieldAndValue eruptFieldAndValue : eruptFieldAndValues) {
                Field field = eruptFieldAndValue.getField();
                field.setAccessible(true);
                EruptField eruptField = field.getAnnotation(EruptField.class);
                if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                    ReferenceTreeType referenceTreeType = eruptField.edit().referenceTreeType();
                    Map<String, Object> treeMap = new HashMap<>();
                    if (null != eruptFieldAndValue.getFieldValue()) {
                        treeMap.put(eruptField.edit().referenceTreeType().id(),
                                ReflectUtil.findClassField(field.get(obj).getClass(), referenceTreeType.id()).get(eruptFieldAndValue.getFieldValue()));
                        treeMap.put(eruptField.edit().referenceTreeType().label(),
                                ReflectUtil.findClassField(field.get(obj).getClass(), referenceTreeType.label()).get(eruptFieldAndValue.getFieldValue()));
                    }
//                    treeMap.put(eruptField.edit().referenceTreeType().pid(),
//                            ReflectUtil.findClassField(field.get(obj).getClass(), referenceTreeType.pid()).get(field.get(obj)));
                    map.put(field.getName(), treeMap);
                } else if (eruptField.edit().type() == EditType.COMBINE
                        || eruptField.edit().type() == EditType.REFERENCE_TABLE) {
                    if (null != eruptFieldAndValue.getFieldValue()) {
                        List<EruptFieldAndValue> subFieldList = new ArrayList<>();
                        findClassAllEruptFields(field.get(obj), subFieldList, OBJECT_EDIT_TYPES);
                        Map<String, Object> subMap = new HashMap<>();
                        for (EruptFieldAndValue subfield : subFieldList) {
                            subMap.put(subfield.getField().getName(), subfield.getFieldValue());
                        }
                        map.put(field.getName(), subMap);
                    }
                } else if (eruptField.edit().type() == EditType.TAB) {
                    continue;
                } else {
                    map.put(field.getName(), field.get(obj));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    //递归查找类字段
    private static void findClassAllEruptFields(Object obj, Consumer<Field> consumer) {
        if (null != obj) {
            for (Field field : obj.getClass().getDeclaredFields()) {
                EruptField eruptField = field.getAnnotation(EruptField.class);
                if (null != eruptField) {
                    consumer.accept(field);
                }
            }
            Class superClass = obj.getClass().getSuperclass();
            if (null != superClass && !superClass.getSimpleName().equals("Object")) {
                findClassAllEruptFields(superClass, consumer);
            }
        }
    }

    //递归查找类字段
    private static void findClassAllEruptFields(Object obj, List<EruptFieldAndValue> fieldAndValueList, EditType... excludeEditType) throws IllegalAccessException {
        if (null != obj) {
            for (Field field : obj.getClass().getDeclaredFields()) {
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
                        field.setAccessible(true);
                        fieldAndValueList.add(new EruptFieldAndValue(field, field.get(obj)));
                    }

                }
            }
            Class superClass = obj.getClass().getSuperclass();
            if (null != superClass && !superClass.getSimpleName().equals("Object")) {
                findClassAllEruptFields(superClass, fieldAndValueList);
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
                return DateUtil.getDate(jsonElement.getAsString());
            case REFERENCE_TREE:
                //TODO 类型转换太频繁,以后优化
                String id = eruptFieldModel.getEruptField().edit().referenceTreeType().id();
                EruptFieldModel efm = CoreService.ERUPTS.get(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(id);
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
