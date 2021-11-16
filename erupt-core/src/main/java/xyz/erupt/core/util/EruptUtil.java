package xyz.erupt.core.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.SneakyThrows;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.SceneEnum;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
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
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

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
                        referMap.put(view.column(), ReflectUtil.findFieldChain(view.column(), value));
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

    public static Map<String, String> getChoiceMap(ChoiceType choiceType) {
        Map<String, String> choiceMap = new LinkedHashMap<>();
        for (xyz.erupt.annotation.sub_field.sub_edit.VL vl : choiceType.vl()) {
            choiceMap.put(vl.value(), vl.label());
        }
        for (Class<? extends ChoiceFetchHandler> clazz : choiceType.fetchHandler()) {
            if (!clazz.isInterface()) {
                List<VLModel> vls = EruptSpringUtil.getBean(clazz).fetch(choiceType.fetchHandlerParams());
                if (null != vls) {
                    for (VLModel vl : vls) {
                        choiceMap.put(vl.getValue(), vl.getLabel());
                    }
                }
            }
        }
        return choiceMap;
    }

    public static List<VLModel> getChoiceList(ChoiceType choiceType) {
        List<VLModel> vls = Stream.of(choiceType.vl()).map(vl -> new VLModel(vl.value(), vl.label(), vl.desc(), vl.disable())).collect(Collectors.toList());
        Stream.of(choiceType.fetchHandler()).filter(clazz -> !clazz.isInterface()).forEach(clazz -> {
            Optional.ofNullable(EruptSpringUtil.getBean(clazz).fetch(choiceType.fetchHandlerParams())).ifPresent(vls::addAll);
        });
        return vls;
    }

    public static List<String> getTagList(TagsType tagsType) {
        List<String> tags = new ArrayList<>(Arrays.asList(tagsType.tags()));
        Stream.of(tagsType.fetchHandler()).filter(clazz -> !clazz.isInterface())
                .forEach(clazz -> tags.addAll(EruptSpringUtil.getBean(clazz).fetchTags(tagsType.fetchHandlerParams())));
        return tags;
    }

    public static Object convertObjectType(EruptFieldModel eruptFieldModel, Object obj) {
        if (null == obj) {
            return null;
        }
        if (null == eruptFieldModel) {
            return obj.toString();
        }
        String str = obj.toString();
        Edit edit = eruptFieldModel.getEruptField().edit();
        switch (edit.type()) {
        case DATE:
            if (Date.class.getSimpleName().equals(eruptFieldModel.getFieldReturnName())) {
                return DateUtil.getDate(str);
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
                    EditTypeSearch editTypeSearch = AnnotationUtil.getEditTypeSearch(edit.type());
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
                    throw new EruptApiErrorTip(EruptApiModel.Status.INFO, edit.title() + "必填", EruptApiModel.PromptWay.MESSAGE);
                }
                if (condition.getValue() instanceof List) {
                    if (((List<?>) condition.getValue()).size() == 0) {
                        throw new EruptApiErrorTip(EruptApiModel.Status.INFO + edit.title() + "必填", EruptApiModel.PromptWay.MESSAGE);
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
                    return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "必填");
                } else if (String.class.getSimpleName().equals(field.getFieldReturnName())) {
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
                    if (!NumberUtils.isCreatable(value.getAsString())) {
                        return EruptApiModel.errorNoInterceptMessage(field.getEruptField().edit().title() + "必须为数值");
                    }
                    break;
                case INPUT:
                    if (!AnnotationConst.EMPTY_STR.equals(edit.inputType().regex())) {
                        String content = value.getAsString();
                        if (StringUtils.isNotBlank(content)) {
                            if (!Pattern.matches(edit.inputType().regex(), content)) {
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

    public static Object toEruptId(EruptModel eruptModel, String id) {
        Field primaryField = ReflectUtil.findClassField(eruptModel.getClazz()
                , eruptModel.getErupt().primaryKeyCol());
        return TypeUtil.typeStrConvertObject(id, primaryField.getType());
    }

    //将对象A的非空数据源覆盖到对象B中
    public static Object dataTarget(EruptModel eruptModel, Object data, Object target, SceneEnum sceneEnum) {
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), (field) -> {
            EruptField eruptField = field.getAnnotation(EruptField.class);
            if (null != eruptField) {
                boolean readonly = sceneEnum == SceneEnum.EDIT ? eruptField.edit().readonly().edit() : eruptField.edit().readonly().add();
                if (StringUtils.isNotBlank(eruptField.edit().title()) && !readonly) {
                    try {
                        Field f = ReflectUtil.findClassField(eruptModel.getClazz(), field.getName());
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
        });
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
     * 获取附件上传代理器
     *
     * @return AttachmentProxy
     */
    public static AttachmentProxy findAttachmentProxy() {
        EruptAttachmentUpload eruptAttachmentUpload = EruptApplication.getPrimarySource().getAnnotation(EruptAttachmentUpload.class);
        if (null != eruptAttachmentUpload) {
            return EruptSpringUtil.getBean(eruptAttachmentUpload.value());
        }
        return null;
    }

}
