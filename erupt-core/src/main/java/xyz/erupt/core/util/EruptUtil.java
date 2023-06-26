package xyz.erupt.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.SceneEnum;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.EditTypeSearch;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.core.annotation.EruptAttachmentUpload;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.proxy.AnnotationProcess;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 11/1/18.
 */
public class EruptUtil {

    //将object中erupt标识的字段抽取出来放到map中
    @SneakyThrows
    public static Map<String, Object> generateEruptDataMap(EruptModel eruptModel, Object obj) {
        Map<String, Object> map = new HashMap<>();
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
                        String id;
                        String label;
                        if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                            ReferenceTreeType referenceTreeType = eruptField.edit().referenceTreeType();
                            id = referenceTreeType.id();
                            label = referenceTreeType.label();
                        } else {
                            ReferenceTableType referenceTableType = eruptField.edit().referenceTableType();
                            id = referenceTableType.id();
                            label = referenceTableType.label();
                        }
                        Map<String, Object> referMap = new HashMap<>();
                        referMap.put(id, ReflectUtil.findFieldChain(id, value));
                        referMap.put(label, ReflectUtil.findFieldChain(label, value));
                        for (View view : eruptField.views()) {
                            //修复表格列无法显示子类属性（例如xxx.yyy.zzz这样的列配置）的缺陷，要配合前端的bug修复。
                            //修复一对多情况下无法显示子类属性的问题 
                            String columnKey = view.column().replace(".", "_");
                            Object columnValue = ReflectUtil.findFieldChain(view.column(), value);
                            referMap.put(columnKey, columnValue);
                            map.put(field.getName() + "_" + columnKey, columnValue);

                        }
                        map.put(field.getName(), referMap);
                        break;
                    case COMBINE:
                        map.put(field.getName(), generateEruptDataMap(EruptCoreService.getErupt(fieldModel.getFieldReturnName()), value));
                        break;
                    case CHECKBOX:
                    case TAB_TREE:
                        EruptModel tabEruptModel = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
                        Collection<?> collection = (Collection<?>) value;
                        if (collection.size() > 0) {
                            Set<Object> idSet = new HashSet<>();
                            Field primaryField = ReflectUtil.findClassField(collection.iterator().next().getClass(),
                                    tabEruptModel.getErupt().primaryKeyCol());
                            for (Object o : collection) {
                                idSet.add(primaryField.get(o));
                            }
                            map.put(field.getName(), idSet);
                        }
                        break;
                    case TAB_TABLE_REFER:
                    case TAB_TABLE_ADD:
                        EruptModel tabEruptModelRef = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
                        Collection<?> collectionRef = (Collection<?>) value;
                        List<Object> list = new ArrayList<>();
                        for (Object o : collectionRef) {
                            list.add(generateEruptDataMap(tabEruptModelRef, o));
                        }
                        map.put(field.getName(), list);
                        break;
                    default:
                        map.put(field.getName(), value);
                        break;
                }
            }
        }
        return map;
    }

    public static Map<String, String> getChoiceMap(EruptModel eruptModel, ChoiceType choiceType) {
        Map<String, String> choiceMap = new LinkedHashMap<>();
        getChoiceList(eruptModel, choiceType).forEach(vl -> choiceMap.put(vl.getValue(), vl.getLabel()));
        return choiceMap;
    }

    public static List<VLModel> getChoiceList(EruptModel eruptModel, ChoiceType choiceType) {
        List<VLModel> vls = Stream.of(choiceType.vl()).map(vl -> new VLModel(vl.value(), vl.label(), vl.desc(), vl.disable())).collect(Collectors.toList());
        Stream.of(choiceType.fetchHandler()).filter(clazz -> !clazz.isInterface()).forEach(clazz ->
                Optional.ofNullable(EruptSpringUtil.getBean(clazz).fetch(choiceType.fetchHandlerParams())).ifPresent(vls::addAll));
        if (eruptModel.isI18n()) {
            vls.forEach(vl -> vl.setLabel(I18nTranslate.$translate(vl.getLabel())));
        }
        return vls;
    }

    public static List<String> getTagList(TagsType tagsType) {
        List<String> tags = new ArrayList<>(Arrays.asList(tagsType.tags()));
        Stream.of(tagsType.fetchHandler()).filter(clazz -> !clazz.isInterface())
                .forEach(clazz -> tags.addAll(EruptSpringUtil.getBean(clazz).fetchTags(tagsType.fetchHandlerParams())));
        return tags;
    }

    public static Object convertObjectType(EruptFieldModel eruptFieldModel, Object obj) {
        if (null == obj) return null;
        if (null == eruptFieldModel) return obj.toString();
        String str = obj.toString();
        Edit edit = eruptFieldModel.getEruptField().edit();
        switch (edit.type()) {
            case DATE:
                if (isDateField(eruptFieldModel.getFieldReturnName())) {
                    return DateUtil.getDate(eruptFieldModel.getField().getType(), str);
                } else {
                    return str;
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
                Map<String, Object> map = (Map<String, Object>) obj;
                return TypeUtil.typeStrConvertObject(map.get(id), efm.getField().getType());
            default:
                return TypeUtil.typeStrConvertObject(str, eruptFieldModel.getField().getType());
        }
    }

    //生成一个合法的searchCondition
    public static List<Condition> geneEruptSearchCondition(EruptModel eruptModel, List<Condition> searchCondition) {
        checkEruptSearchNotnull(eruptModel, searchCondition);
        List<Condition> legalConditions = new ArrayList<>();
        if (null != searchCondition) {
            for (Condition condition : searchCondition) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(condition.getKey());
                if (null != eruptFieldModel) {
                    Edit edit = eruptFieldModel.getEruptField().edit();
                    EditTypeSearch editTypeSearch = AnnotationProcess.getEditTypeSearch(edit.type());
                    if (null != editTypeSearch && editTypeSearch.value()) {
                        if (edit.search().value() && null != condition.getValue()) {
                            if (condition.getValue() instanceof Collection) {
                                Collection<?> collection = (Collection<?>) condition.getValue();
                                if (collection.size() == 0) {
                                    continue;
                                }
                            }
                            if (edit.search().vague()) {
                                condition.setExpression(editTypeSearch.vagueMethod());
                            } else {
                                condition.setExpression(QueryExpression.EQ);
                            }
                            legalConditions.add(condition);
                        }
                    }
                }
            }
        }
        return legalConditions;
    }

    public static void checkEruptSearchNotnull(EruptModel eruptModel, List<Condition> searchCondition) {
        Map<String, Condition> conditionMap = new HashMap<>();
        if (null != searchCondition) {
            searchCondition.forEach(condition -> conditionMap.put(condition.getKey(), condition));
        }
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.search().value() && edit.search().notNull()) {
                Condition condition = conditionMap.get(fieldModel.getFieldName());
                if (null == condition || null == condition.getValue()) {
                    throw new EruptApiErrorTip(EruptApiModel.Status.INFO, edit.title() + " " + I18nTranslate.$translate("erupt.notnull"), EruptApiModel.PromptWay.MESSAGE);
                }
                if (condition.getValue() instanceof List) {
                    if (((List<?>) condition.getValue()).size() == 0) {
                        throw new EruptApiErrorTip(EruptApiModel.Status.INFO + edit.title() + " " + I18nTranslate.$translate("erupt.notnull"), EruptApiModel.PromptWay.MESSAGE);
                    }
                }
            }
        }
    }

    public static EruptApiModel validateEruptValue(EruptModel eruptModel, JsonObject jsonObject) {
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            Edit edit = field.getEruptField().edit();
            JsonElement value = jsonObject.get(field.getFieldName());
            if (field.getEruptField().edit().notNull()) {
                if (null == value || value.isJsonNull()) {
                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.notnull"));
                } else if (String.class.getSimpleName().equals(field.getFieldReturnName())) {
                    if (StringUtils.isBlank(value.getAsString())) {
                        return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.notnull"));
                    }
                }
            }
            if (field.getEruptField().edit().type() == EditType.COMBINE) {
                EruptApiModel eam = validateEruptValue(EruptCoreService.getErupt(field.getFieldReturnName()), jsonObject.getAsJsonObject(field.getFieldName()));
                if (eam.getStatus() == EruptApiModel.Status.ERROR) {
                    return eam;
                }
            }
            if (null != value && !AnnotationConst.EMPTY_STR.equals(edit.title())) {
                //xss 注入处理
                if (edit.type() == EditType.TEXTAREA || edit.type() == EditType.INPUT) {
                    if (SecurityUtil.xssInspect(value.getAsString())) {
                        return EruptApiModel.errorNoInterceptApi(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.attack.xss"));
                    }
                }
                //数据类型校验
                switch (edit.type()) {
                    case NUMBER:
                    case SLIDER:
                        if (!NumberUtils.isCreatable(value.getAsString())) {
                            return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.must.number"));
                        }
                        break;
                    case INPUT:
                        if (!AnnotationConst.EMPTY_STR.equals(edit.inputType().regex())) {
                            String content = value.getAsString();
                            if (StringUtils.isNotBlank(content)) {
                                if (!Pattern.matches(edit.inputType().regex(), content)) {
                                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + " " + I18nTranslate.$translate("erupt.incorrect_format"));
                                }
                            }
                        }
                        break;
                }
            }
        }
        return EruptApiModel.successApi();
    }

    /**
     * 前端数据处理逻辑
     */
    public static void processEruptWebValue(EruptModel eruptModel, JsonObject jsonObject) {
        for (EruptFieldModel field : eruptModel.getEruptFieldModels()) {
            JsonElement value = jsonObject.get(field.getFieldName());
            Edit edit = field.getEruptField().edit();
            if (null != value && !value.isJsonNull()) {
                //代码编辑器类型加密传输后解密
                if (edit.type() == EditType.CODE_EDITOR) {
                    jsonObject.addProperty(field.getFieldName(), SecretUtil.decodeSecret(value.getAsString()));
                } else {
                    if (value.isJsonObject() && edit.type() == EditType.COMBINE) {
                        processEruptWebValue(EruptCoreService.getErupt(field.getFieldReturnName()), value.getAsJsonObject());
                    } else if (value.isJsonArray()) {
                        switch (edit.type()) {
                            case TAB_TABLE_ADD:
                            case TAB_TABLE_REFER:
                                value.getAsJsonArray().forEach(jsonElement ->
                                        Optional.ofNullable(EruptCoreService.getErupt(field.getFieldReturnName())).ifPresent(it -> {
                                            processEruptWebValue(it, jsonElement.getAsJsonObject());
                                        })
                                );
                                break;
                        }
                    }
                }
                //TODO 密码组件值加密传输后解密
            }
        }
    }

    public static Object toEruptId(EruptModel eruptModel, String id) {
        Field primaryField = ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol());
        return TypeUtil.typeStrConvertObject(id, primaryField.getType());
    }

    //将对象A的非空数据源覆盖到对象B中
    public static Object dataTarget(EruptModel eruptModel, Object data, Object target, SceneEnum sceneEnum) {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            EruptField eruptField = fieldModel.getEruptField();
            boolean readonly = sceneEnum == SceneEnum.EDIT ? eruptField.edit().readonly().edit() : eruptField.edit().readonly().add();
            if (StringUtils.isNotBlank(eruptField.edit().title()) && !readonly) {
                Field f = fieldModel.getField();
                try {
                    f.setAccessible(true);
                    if (eruptField.edit().type() == EditType.TAB_TABLE_ADD) {
                        Collection<?> s = (Collection<?>) f.get(target);
                        if (null == s) {
                            f.set(target, f.get(data));
                        } else {
                            s.clear();
                            s.addAll((Collection) f.get(data));
                            f.set(target, s);
                        }
                    } else {
                        f.set(target, f.get(data));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return target;
    }

    //清理序列化后对象所产生的默认值（通过json串进行校验）
    public static void clearObjectDefaultValueByJson(Object obj, JsonObject data) {
        ReflectUtil.findClassAllFields(obj.getClass(), field -> {
            try {
                field.setAccessible(true);
                if (null != field.get(obj)) {
                    if (!data.has(field.getName())) {
                        field.set(obj, null);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 将JSON串转换为erupt实体对象
     *
     * @param json      json对象
     * @param extraData 额外填充的反射数据
     */
    public static Object jsonToEruptEntity(EruptModel eruptModel, JsonObject json, Map<String, Object> extraData) throws InstantiationException, IllegalAccessException {
        Gson gson = GsonFactory.getGson();
        Object o = gson.fromJson(json.toString(), eruptModel.getClazz());
        EruptUtil.clearObjectDefaultValueByJson(o, json);
        Object obj = EruptUtil.dataTarget(eruptModel, o, eruptModel.getClazz().newInstance(), SceneEnum.ADD);
        if (null != extraData) {
            for (String key : extraData.keySet()) {
                Field field = ReflectUtil.findClassField(eruptModel.getClazz(), key);
                field.setAccessible(true);
                field.set(obj, gson.fromJson(extraData.get(key).toString(), field.getType()));
            }
        }
        return obj;
    }

    /**
     * 获取附件上传代理器
     *
     * @return AttachmentProxy
     */
    public static AttachmentProxy findAttachmentProxy() {
        EruptAttachmentUpload eruptAttachmentUpload = EruptApplication.getPrimarySource().getAnnotation(EruptAttachmentUpload.class);
        return null == eruptAttachmentUpload ? null : EruptSpringUtil.getBean(eruptAttachmentUpload.value());
    }

    //是否为时间字段
    public static boolean isDateField(String fieldType) {
        if (Date.class.getSimpleName().equals(fieldType)) {
            return true;
        } else if (LocalDate.class.getSimpleName().equals(fieldType)) {
            return true;
        } else {
            return LocalDateTime.class.getSimpleName().equals(fieldType);
        }
    }

}
