package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DateEnum;
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
                Field field = fieldModel.getField();
                field.setAccessible(true);
                Object value = field.get(obj);
                if (null != value) {
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
                            referMap.put(id, ReflectUtil.findClassField(value.getClass(), id).get(value));
                            referMap.put(label, ReflectUtil.findClassField(value.getClass(), label).get(value));
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
                                map.put(field.getName(), list);
                            }
                            break;
                        default:
                            map.put(field.getName(), value);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
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
            case REFERENCE_TABLE:
                String id = null;
                if (eruptFieldModel.getEruptField().edit().type().equals(EditType.REFERENCE_TREE)) {
                    id = eruptFieldModel.getEruptField().edit().referenceTreeType().id();
                } else {
                    id = eruptFieldModel.getEruptField().edit().referenceTableType().id();
                }
                EruptFieldModel efm = CoreService.getErupt(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(id);
                //TODO 类型转换太频繁,以后优化
                return TypeUtil.typeStrConvertObject(jsonElement.getAsJsonObject().get(id).getAsString(), efm.getField().getType().getSimpleName());
            default:
                return jsonElement.getAsString();
        }
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
