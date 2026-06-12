package xyz.erupt.designer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptBuildModel;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.designer.model.DesignerEntity;
import xyz.erupt.designer.model.DesignerForm;
import xyz.erupt.designer.proxy.JsonAnnotationProxy;
import xyz.erupt.designer.template.EruptDesignerTemplate;
import xyz.erupt.jpa.dao.EruptDao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

/**
 * Convert designer json to a runtime {@link EruptModel}: the template class supplies real
 * annotation instances, {@link JsonAnnotationProxy} disguises them with designer values,
 * so the rest of the framework consumes the design as if it were a hand-written @Erupt class
 * — no compilation, no restart.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Slf4j
@Service
public class EruptDesignerService {

    @Resource
    private EruptDao eruptDao;

    @SneakyThrows
    public EruptModel toEruptModel(DesignerForm form) {
        // 模板类提供注解实例来源；真实承载类由字节码动态生成（真实字段 → Gson/反射全链路可用）
        Class<?> dynamicClass = DesignerClassFactory.build(form);
        EruptModel model = new EruptModel(EruptDesignerTemplate.class);
        model.setClazz(dynamicClass);
        model.setEruptName(Optional.ofNullable(form.getClassName()).orElse(EruptDesignerTemplate.class.getSimpleName()));
        Optional.ofNullable(form.getErupt()).ifPresent(it -> model.setErupt(JsonAnnotationProxy.proxy(model.getErupt(), it)));
        model.setEruptFieldModels(new ArrayList<>());
        model.setEruptFieldMap(new LinkedCaseInsensitiveMap<>());
        // BaseModel.id 自带 @EruptField，保证主键参与序列化与表单回显
        EruptFieldModel idField = new EruptFieldModel(xyz.erupt.jpa.model.BaseModel.class.getDeclaredField("id"), false);
        model.getEruptFieldModels().add(idField);
        model.getEruptFieldMap().put(idField.getFieldName(), idField);
        int sort = 0;
        for (DesignerForm.DesignerField designerField : Optional.ofNullable(form.getFields()).orElse(new ArrayList<>())) {
            EditType editType = Optional.ofNullable(designerField.getEdit())
                    .filter(it -> it.has("type"))
                    .map(it -> EditType.valueOf(it.get("type").getAsString())).orElse(EditType.INPUT);
            EruptFieldModel fieldModel = new EruptFieldModel(this.templateField(editType), false);
            fieldModel.setField(dynamicClass.getDeclaredField(designerField.getFieldName()));
            fieldModel.setFieldName(designerField.getFieldName());
            fieldModel.setFieldReturnName(this.fieldReturnName(designerField, editType));
            JsonObject fieldJson = new JsonObject();
            JsonArray views = new JsonArray();
            Optional.ofNullable(designerField.getView()).ifPresent(views::add);
            fieldJson.add("views", views);
            Optional.ofNullable(designerField.getEdit()).ifPresent(it -> fieldJson.add("edit", it));
            fieldJson.addProperty("sort", sort += 10);
            fieldModel.setEruptField(JsonAnnotationProxy.proxy(fieldModel.getEruptField(), fieldJson));
            model.getEruptFieldModels().add(fieldModel);
            model.getEruptFieldMap().put(fieldModel.getFieldName(), fieldModel);
        }
        return model;
    }

    // 发布：保存设计配置 → 注册运行时模型，免重启生效
    @Transactional
    public void publish(String className, DesignerForm form) {
        DesignerEntity entity = eruptDao.lambdaQuery(DesignerEntity.class)
                .eq(DesignerEntity::getClassName, className).one();
        if (null == entity) throw new EruptWebApiRuntimeException("模型不存在：" + className);
        form.setClassName(entity.getClassName());
        if (null == form.getErupt() || !form.getErupt().has("name")) {
            throw new EruptWebApiRuntimeException("请填写功能名称");
        }
        EruptModel model = this.toEruptModel(form);
        entity.setConfig(GsonFactory.getGson().toJson(form));
        entity.setName(form.getErupt().get("name").getAsString());
        entity.setPublishTime(new Date());
        entity.setUpdateTime(new Date());
        eruptDao.merge(entity);
        EruptCoreService.registerErupt(model);
    }

    public DesignerEntity loadDesign(String className) {
        DesignerEntity entity = eruptDao.lambdaQuery(DesignerEntity.class)
                .eq(DesignerEntity::getClassName, className).one();
        if (null == entity) throw new EruptWebApiRuntimeException("模型不存在：" + className);
        return entity;
    }

    // 启动时重注册全部已发布设计
    public void registerAll() {
        for (DesignerEntity entity : eruptDao.lambdaQuery(DesignerEntity.class).list()) {
            if (null == entity.getConfig() || entity.getConfig().isEmpty()) continue;
            try {
                EruptCoreService.registerErupt(this.toEruptModel(
                        GsonFactory.getGson().fromJson(entity.getConfig(), DesignerForm.class)));
            } catch (Exception e) {
                log.warn("Designer model register failed: {} → {}", entity.getClassName(), e.getMessage());
            }
        }
    }

    @SneakyThrows
    public EruptBuildModel preview(DesignerForm form) {
        EruptModel model = this.toEruptModel(form).clone();
        for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE || edit.type() == EditType.MULTI_CHOICE) {
                fieldModel.setComponentValue(EruptUtil.getChoiceList(model, edit));
            }
        }
        EruptBuildModel buildModel = new EruptBuildModel();
        buildModel.setEruptModel(model);
        buildModel.setPower(new PowerObject(model.getErupt().power()));
        return buildModel;
    }

    @SneakyThrows
    private Field templateField(EditType editType) {
        switch (editType) {
            case NUMBER:
            case SLIDER:
            case RATE:
                return EruptDesignerTemplate.class.getDeclaredField("numberValue");
            case BOOLEAN:
                return EruptDesignerTemplate.class.getDeclaredField("boolValue");
            case DATE:
                return EruptDesignerTemplate.class.getDeclaredField("dateValue");
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
            case COMBINE:
                return EruptDesignerTemplate.class.getDeclaredField("refValue");
            case CHECKBOX:
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                return EruptDesignerTemplate.class.getDeclaredField("setValue");
            default:
                return EruptDesignerTemplate.class.getDeclaredField("stringValue");
        }
    }

    private String fieldReturnName(DesignerForm.DesignerField field, EditType editType) {
        switch (editType) {
            case NUMBER:
            case SLIDER:
            case RATE:
                return EruptFieldModel.NUMBER;
            case BOOLEAN:
                return "Boolean";
            case DATE:
                return "Date";
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
            case COMBINE:
            case CHECKBOX:
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
                return Optional.ofNullable(field.getLinkErupt()).filter(it -> !it.isEmpty())
                        .filter(it -> null != EruptCoreService.getErupt(it))
                        .orElseThrow(() -> new EruptWebApiRuntimeException(
                                "Field '" + field.getFieldName() + "' requires a registered erupt class to link"));
            default:
                return "String";
        }
    }

}
