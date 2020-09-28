package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.fun.VL;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author liyuepeng
 * @date 11/1/18.
 */
public class EruptUtil {

    private static final EditType[] OBJECT_EDIT_TYPES = {EditType.COMBINE, EditType.REFERENCE_TREE, EditType.REFERENCE_TABLE,
            EditType.TAB_TREE, EditType.TAB_TABLE_ADD, EditType.TAB_TABLE_REFER};

    //将object中erupt标识的字段抽取出来放到map中
    public static Map<String, Object> generateEruptDataMap(EruptModel eruptModel, Object obj) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
                if (AnnotationConst.EMPTY_STR.equals(fieldModel.getEruptField().edit().title()) &&
                        !eruptModel.getErupt().primaryKeyCol().equals(fieldModel.getFieldName())) {
                    continue;
                }
                Field field = fieldModel.getField();
                field.setAccessible(true);
                Object value = field.get(obj);
                if (null != value) {
                    EruptField eruptField = fieldModel.getEruptField();
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
                            referMap.put(id, ReflectUtil.findFieldChain(id, value));
                            referMap.put(label, ReflectUtil.findFieldChain(label, value));
                            for (View view : eruptField.views()) {
                                referMap.put(view.column(), ReflectUtil.findFieldChain(view.column(), value));
                            }
                            map.put(field.getName(), referMap);
                            break;
                        case COMBINE:
                            map.put(field.getName(), generateEruptDataMap(EruptCoreService.getErupt(fieldModel.getFieldReturnName()), value));
                            break;
                        case TAB_TREE:
                        case TAB_TABLE_REFER:
                        case TAB_TABLE_ADD:
                            EruptModel tabEruptModel = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
                            Collection collection = (Collection) value;
                            if (eruptField.edit().type() == EditType.TAB_TREE) {
                                if (collection.size() > 0) {
                                    Set<String> idSet = new HashSet<>();
                                    Field primaryField = ReflectUtil.findClassField(collection.iterator().next().getClass(),
                                            tabEruptModel.getErupt().primaryKeyCol());
                                    for (Object o : collection) {
                                        idSet.add(primaryField.get(o).toString());
                                    }
                                    map.put(field.getName(), idSet);
                                }
                            } else {
                                List<Object> list = new ArrayList<>();
                                for (Object o : collection) {
                                    list.add(generateEruptDataMap(tabEruptModel, o));
                                }
                                map.put(field.getName(), list);
                            }
                            break;
                        default:
                            map.put(field.getName(), value);
                            break;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static Map<String, String> getChoiceMap(ChoiceType choiceType) {
        Map<String, String> choiceMap = new LinkedHashMap<>();
        for (xyz.erupt.annotation.sub_field.sub_edit.VL vl : choiceType.vl()) {
            choiceMap.put(vl.value(), vl.label());
        }
        for (Class<? extends ChoiceFetchHandler> cla : choiceType.fetchHandler()) {
            List<VL> vls = EruptSpringUtil.getBean(cla).fetch(choiceType.fetchHandlerParams());
            if (null != vls) {
                for (VL vl : vls) {
                    choiceMap.put(vl.getValue(), vl.getLabel());
                }
            }

        }
        return choiceMap;
    }

    public static List<VL> getChoiceList(ChoiceType choiceType) {
        List<VL> vls = new ArrayList<>();
        for (xyz.erupt.annotation.sub_field.sub_edit.VL vl : choiceType.vl()) {
            vls.add(new VL(vl.value(), vl.label(), vl.desc()));
        }
        for (Class<? extends ChoiceFetchHandler> cla : choiceType.fetchHandler()) {
            List<VL> VL = EruptSpringUtil.getBean(cla).fetch(choiceType.fetchHandlerParams());
            if (null != VL) {
                vls.addAll(VL);
            }
        }
        return vls;
    }

    //请求参数转换
    public static Object jsonElementToObject(EruptFieldModel eruptFieldModel, JsonElement jsonElement) {
        if (null == eruptFieldModel) {
            return jsonElement.getAsString();
        }
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            List list = new ArrayList();
            for (JsonElement element : jsonArray) {
                if (JavaType.NUMBER.equals(eruptFieldModel.getFieldReturnName())) {
                    list.add(TypeUtil.typeStrConvertObject(jsonElement.getAsString(), eruptFieldModel.getFieldReturnName()));
                } else {
                    list.add(element.getAsString());
                }
            }
            return list;
        }
        if (JavaType.NUMBER.equalsIgnoreCase(eruptFieldModel.getFieldReturnName())) {
            return TypeUtil.typeStrConvertObject(jsonElement.getAsString(), eruptFieldModel.getField().getType().getSimpleName());
        }
        Edit edit = eruptFieldModel.getEruptField().edit();
        switch (edit.type()) {
            case SLIDER:
                return jsonElement.getAsInt();
            case BOOLEAN:
                return jsonElement.getAsBoolean();
            case DATE:
                if (JavaType.DATE.equals(eruptFieldModel.getFieldReturnName())) {
                    return DateUtil.getDate(jsonElement.getAsString());
                } else {
                    return jsonElement.getAsString();
                }
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
                String id = null;
                if (edit.type().equals(EditType.REFERENCE_TREE)) {
                    id = eruptFieldModel.getEruptField().edit().referenceTreeType().id();
                } else if (edit.type().equals(EditType.REFERENCE_TABLE)) {
                    id = edit.referenceTableType().id();
                }
                EruptFieldModel efm = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(id);
                return TypeUtil.typeStrConvertObject(jsonElement.getAsJsonObject().get(id).getAsString(), efm.getField().getType().getSimpleName());
            default:
                return jsonElement.getAsString();
        }
    }

    //生成一个合法的searchContidion
    public static JsonObject geneEruptSearchCondition(EruptModel eruptModel, JsonObject searchCondition) {
        JsonObject legalJsonObject = new JsonObject();
        if (null != searchCondition) {
            for (String key : searchCondition.keySet()) {
                if (!eruptModel.getEruptFieldMap().containsKey(key)) {
                    continue;
                }
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(key);
                Edit edit = eruptFieldModel.getEruptField().edit();
                if (AnnotationUtil.getEditTypeMapping(edit.type()).search()) {
                    if (edit.search().value() && !searchCondition.get(key).isJsonNull()) {
                        if (searchCondition.get(key).isJsonArray()) {
                            if (searchCondition.get(key).getAsJsonArray().size() == 0) {
                                continue;
                            }
                        } else if (JavaType.STRING.equals(eruptFieldModel.getFieldReturnName())) {
                            if (StringUtils.isBlank(searchCondition.get(key).toString())) {
                                continue;
                            }
                        }
                        legalJsonObject.add(key, searchCondition.get(key));
                    }
                }
            }
        }
        return legalJsonObject;
    }

    public static EruptApiModel validateEruptValue(EruptModel eruptModel, JsonObject jsonObject) {
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            Edit edit = field.getEruptField().edit();
            JsonElement value = jsonObject.get(field.getFieldName());
//            if (field.getEruptField().edit().readOnly()) {
//                if (null != value) {
//                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "只读");
//                }
//            }
            if (field.getEruptField().edit().notNull()) {
                if (null == value) {
                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "必填");
                } else if (JavaType.STRING.equals(field.getFieldReturnName())) {
                    if (StringUtils.isBlank(value.getAsString())) {
                        return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "必填");
                    }
                }
            }
            if (field.getEruptField().edit().type() == EditType.COMBINE) {
                EruptApiModel eam = validateEruptValue(EruptCoreService.getErupt(field.getFieldReturnName()), jsonObject.getAsJsonObject(field.getFieldName()));
                if (eam.getStatus() == EruptApiModel.Status.ERROR) {
                    return eam;
                }
            }

            if (null != value) {
                //xss 注入处理
                if (edit.type() == EditType.TEXTAREA || edit.type() == EditType.INPUT) {
                    if (SecurityUtil.xssInspect(value.getAsString())) {
                        return EruptApiModel.errorNoInterceptApi(field.getEruptField().edit().title() + "检测到有恶意跨站脚本，请重新编辑！");
                    }
                }
                //数据类型校验
                switch (edit.type()) {
                    case NUMBER:
                    case SLIDER:
                        if (!TypeUtil.isNumeric(value.getAsString())) {
                            return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "必须为数值");
                        }
                        break;
                    case INPUT:
                        if (!AnnotationConst.EMPTY_STR.equals(edit.inputType().regex())) {
                            String content = value.getAsString();
                            if (StringUtils.isNotBlank(content)) {
                                boolean isMatch = Pattern.matches(edit.inputType().regex(), content);
                                if (!isMatch) {
                                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "格式不正确");
                                }
                            }
                        }
                        break;

                }
            }
        }
        return EruptApiModel.successApi();
    }

    //处理dataProxy回调对象
    public static void handlerDataProxy(EruptModel eruptModel, Consumer<DataProxy> consumer) {
        PreDataProxy preDataProxy = eruptModel.getClazz().getAnnotation(PreDataProxy.class);
        if (null != preDataProxy) {
            consumer.accept(EruptSpringUtil.getBean(preDataProxy.value()));
        }
        for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dataProxy()) {
            consumer.accept(EruptSpringUtil.getBean(proxy));
        }
    }

    //动态获取erupt power值
    public static PowerObject getPowerObject(EruptModel eruptModel) {
        Power power = eruptModel.getErupt().power();
        PowerObject powerBean = new PowerObject(power);
        if (!power.powerHandler().isInterface()) {
            EruptSpringUtil.getBean(power.powerHandler()).handler(powerBean);
        }
        return powerBean;
    }

    public static Object toEruptId(EruptModel eruptModel, String id) {
//        eruptModel.getEruptFieldMap().get(eruptModel.getErupt().primaryKeyCol());
        Field primaryField = ReflectUtil.findClassField(eruptModel.getClazz()
                , eruptModel.getErupt().primaryKeyCol());
        return TypeUtil.typeStrConvertObject(id, primaryField.getType().getSimpleName().toLowerCase());
    }

    public static Object findEruptObjectFieldId(EruptModel eruptModel, String field, Object val) {
        String fr = eruptModel.getEruptFieldMap().get(field).getFieldReturnName();
        EruptModel em = EruptCoreService.getErupt(fr);
        return toEruptId(em, val.toString());
    }

}
