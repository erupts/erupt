package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.core.bean.EruptApiModel;
import xyz.erupt.core.bean.EruptFieldModel;
import xyz.erupt.core.bean.EruptModel;
import xyz.erupt.core.service.CoreService;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by liyuepeng on 11/1/18.
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
                            referMap.put(id, ReflectUtil.findClassField(value.getClass(), id).get(value));
                            referMap.put(label, ReflectUtil.findClassField(value.getClass(), label).get(value));
                            for (View view : eruptField.views()) {
                                referMap.put(view.column(), ReflectUtil.findClassField(value.getClass(), view.column()).get(value));
                            }
                            map.put(field.getName(), referMap);
                            break;
                        case COMBINE:
                            map.put(field.getName(), generateEruptDataMap(CoreService.getErupt(fieldModel.getFieldReturnName()), value));
                            break;
                        case TAB_TREE:
                        case TAB_TABLE_REFER:
                        case TAB_TABLE_ADD:
                            EruptModel tabEruptModel = CoreService.getErupt(fieldModel.getFieldReturnName());
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
                                List list = new ArrayList();
                                for (Object o : collection) {
                                    list.add(generateEruptDataMap(tabEruptModel, o));
                                }
//                                for (Class<? extends DataProxy> proxy : tabEruptModel.getErupt().dateProxy()) {
//                                    SpringUtil.getBean(proxy).afterFetch(list);
//                                }
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

    //请求参数转换
    public static Object jsonElementToObject(EruptFieldModel eruptFieldModel, JsonElement jsonElement) {
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
        switch (eruptFieldModel.getEruptField().edit().type()) {
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
                if (eruptFieldModel.getEruptField().edit().type().equals(EditType.REFERENCE_TREE)) {
                    id = eruptFieldModel.getEruptField().edit().referenceTreeType().id();
                } else if (eruptFieldModel.getEruptField().edit().type().equals(EditType.REFERENCE_TABLE)) {
                    id = eruptFieldModel.getEruptField().edit().referenceTableType().id();
                }
                EruptFieldModel efm = CoreService.getErupt(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(id);
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
            if (field.getEruptField().edit().notNull()) {
                if (!jsonObject.has(field.getFieldName())) {
                    EruptApiModel eruptApiModel = EruptApiModel.errorApi(field.getEruptField().edit().title() + "必填");
                    eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
                    eruptApiModel.setErrorIntercept(false);
                    return eruptApiModel;
                }
                if (JavaType.STRING.equals(field.getFieldReturnName())) {
                    String str = jsonObject.get(field.getFieldName()).getAsString();
                    if (StringUtils.isBlank(str)) {
                        EruptApiModel eruptApiModel = EruptApiModel.errorApi(field.getEruptField().edit().title() + "必填");
                        eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
                        eruptApiModel.setErrorIntercept(false);
                        return eruptApiModel;
                    }
                }
            }
            if (field.getEruptField().edit().type() == EditType.COMBINE) {
                EruptApiModel eam = validateEruptValue(CoreService.getErupt(field.getFieldReturnName()), jsonObject.getAsJsonObject(field.getFieldName()));
                if (eam.getStatus() == EruptApiModel.Status.ERROR) {
                    return eam;
                }
            }
            Edit edit = field.getEruptField().edit();
            switch (edit.type()) {
                case INPUT:
                    if (!AnnotationConst.EMPTY_STR.equals(edit.inputType().regex())) {
                        if (jsonObject.has(field.getFieldName())) {
                            String content = jsonObject.get(field.getFieldName()).getAsString();
                            if (StringUtils.isNotBlank(content)) {
                                boolean isMatch = Pattern.matches(edit.inputType().regex(), content);
                                if (!isMatch) {
                                    EruptApiModel eruptApiModel = EruptApiModel.errorApi(field.getEruptField().edit().title() + "格式不正确");
                                    eruptApiModel.setPromptWay(EruptApiModel.PromptWay.MESSAGE);
                                    eruptApiModel.setErrorIntercept(false);
                                    return eruptApiModel;
                                }
                            }
                        }
                    }
                    break;

            }
        }
        return EruptApiModel.successApi();
    }
}
