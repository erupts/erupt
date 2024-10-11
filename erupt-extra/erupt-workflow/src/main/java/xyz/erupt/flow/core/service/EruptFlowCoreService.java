package xyz.erupt.flow.core.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.service.EruptApplication;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.flow.core.annotation.EruptFlowForm;
import xyz.erupt.flow.core.view.EruptFormModel;

import java.math.BigDecimal;
import java.util.*;

@Order(1000)
@Service
@Slf4j
public class EruptFlowCoreService implements ApplicationRunner {

    private static final Map<String, EruptFormModel> ERUPT_FLOW_FORM_MAP = new LinkedCaseInsensitiveMap<>();

    private static final List<EruptFormModel> ERUPT_FLOW_FORM_LIST = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TimeRecorder totalRecorder = new TimeRecorder();
        /*
         * 扫描所有@EruptFlowForm注解
         * 将其解析为表单对象（EruptFormModel），保存到缓存中（ERUPT_FLOW_MAP、ERUPT_FLOW_LIST）
         */
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(EruptFlowForm.class)
        }, clazz -> {
            Optional.ofNullable(EruptCoreService.getErupt(clazz.getSimpleName())).ifPresent(eruptModel -> {
                EruptFormModel eruptFormModel = new EruptFormModel(eruptModel);
                eruptFormModel.setFormItems(this.parseFormItems(eruptModel));
                ERUPT_FLOW_FORM_MAP.put(clazz.getSimpleName(), eruptFormModel);
                ERUPT_FLOW_FORM_LIST.add(eruptFormModel);
            });
        });
        log.info("<" + repeat("---", 20) + ">");
        log.info("Erupt Flow Form classes : " + ERUPT_FLOW_FORM_MAP.size());
        log.info("Erupt Flow Form initialization completed in {}ms", totalRecorder.recorder());
        log.info("<" + repeat("---", 20) + ">");
    }

    /**
     * 将erupt注解，解析为表单项
     */
    private JSONArray parseFormItems(EruptModel eruptModel) {
        try {
            JSONArray jsons = new JSONArray();
            eruptModel.getEruptFieldMap().forEach((key, fieldModel) -> {
                if (fieldModel.getEruptField().edit().title().length() <= 0) {
                    return;//只处理有标题的字段
                }
                jsons.add(this.buildItem(fieldModel, fieldModel.getEruptField().edit()));
            });
            return jsons;
        } catch (Exception e) {
            throw new RuntimeException("Load Erupt Form failed: " + eruptModel.getEruptName());
        }
    }

    private JSONObject buildItem(EruptFieldModel fieldModel, Edit edit) {
        JSONObject json = new JSONObject();
        json.put("id", fieldModel.getField().getName());//id=字段名
        json.put("title", edit.title());//title=编辑的title
        json.put("placeholder", edit.placeHolder());
        String componentName = this.convertComponentName(fieldModel, edit);
        json.put("name", componentName);
        json.put("props", this.buildProps(componentName, fieldModel, edit));
        return json;
    }

    /**
     * 根据字段配置解析对应的组件
     *
     * @return
     */
    public String convertComponentName(EruptFieldModel fieldModel, Edit edit) {
        switch (edit.type()) {
            case INPUT://文本输入
                return "TextInput";
            case NUMBER://数字输入
            case SLIDER://数字滑块
                if (
                        Double.class == fieldModel.getField().getType() || double.class == fieldModel.getField().getType()
                                || Float.class == fieldModel.getField().getType() || float.class == fieldModel.getField().getType()
                                || BigDecimal.class == fieldModel.getField().getType()
                ) {//几种浮点数
                    return "AmountInput";
                } else {//其他的用数字输入框
                    return "NumberInput";
                }
            case DATE://日期
                return "DateTime";
            case BOOLEAN://boolean值，用展开的单选框代替
                return "SelectInput";
            case CHOICE://选择框
                return "SelectInput";
            case TAGS://标签选择器
                return "MultipleSelect";
            case AUTO_COMPLETE://自动完成
                throw new RuntimeException("Can not convert the Edit Type: " + edit.type());
            case TEXTAREA://多行文本
                return "TextareaInput";
            case HTML_EDITOR://网页编辑器
            case CODE_EDITOR://代码编辑器
            case MARKDOWN://MD编辑器
                //这几种只能用多行文本编辑器
                return "TextareaInput";
            case ATTACHMENT://附件
                return "FileUpload";
            case AUTO:
                return this.convertComponentNameByField(fieldModel, edit);
            case MAP://地图
            case TPL://模板
            case DIVIDE://横向分割线
            case HIDDEN://隐藏域
            case EMPTY://空值
            default:
                throw new RuntimeException("Can not convert the Edit Type:" + edit.type());
        }
    }

    public String convertComponentNameByField(EruptFieldModel fieldModel, Edit edit) {
        Class<?> aClass = fieldModel.getField().getType();
        if (String.class.equals(aClass)) {//文本输入
            return "TextInput";
        } else if (Double.class.equals(aClass) || double.class.equals(aClass) || Float.class.equals(aClass) || float.class.equals(aClass) || BigDecimal.class.equals(aClass)) {//浮点数输入
            return "AmountInput";
        } else if (Short.class.equals(aClass) || short.class.equals(aClass) || Integer.class.equals(aClass) || int.class.equals(aClass) || Long.class.equals(aClass) || long.class.equals(aClass)) {//数字输入
            return "NumberInput";
        } else if (Date.class.equals(aClass)) {//日期
            return "DateTime";
        } else if (Boolean.class.equals(aClass) || boolean.class.equals(aClass)) {//boolean值，用展开的单选框代替
            return "SelectInput";
        } else {
            throw new RuntimeException("Can not convert the Field Type:" + edit.type());
        }
    }

    private JSONObject buildProps(String componentName, EruptFieldModel fieldModel, Edit edit) {
        JSONObject json = new JSONObject();
        json.put("required", edit.notNull());
        if ("AmountInput".equals(componentName)) {
            json.put("min", edit.numberType().min());
            json.put("max", edit.numberType().max());
            json.put("showChinese", false);
        } else if ("DateTime".equals(componentName)) {
            if (DateType.Type.DATE.equals(edit.dateType().type())) {
                json.put("format", "yyyy-MM-dd");
            } else if (DateType.Type.TIME.equals(edit.dateType().type())) {
                json.put("format", "HH:mm:ss");
            } else if (DateType.Type.DATE_TIME.equals(edit.dateType().type())) {
                json.put("format", "yyyy-MM-dd HH:mm:ss");
            } else if (DateType.Type.WEEK.equals(edit.dateType().type())) {
                throw new RuntimeException("Can not resolve DateType: " + fieldModel.getFieldName());
            } else if (DateType.Type.MONTH.equals(edit.dateType().type())) {
                json.put("format", "yyyy-MM");
            } else if (DateType.Type.YEAR.equals(edit.dateType().type())) {
                json.put("format", "yyyy");
            } else {
                throw new RuntimeException("Can not resolve DateType: " + fieldModel.getFieldName());
            }
        } else if ("SelectInput".equals(componentName)) {
            if (
                    EditType.BOOLEAN.equals(edit.type())
                            || Boolean.class.equals(fieldModel.getField().getType())
                            || boolean.class.equals(fieldModel.getField().getType())
            ) {
                json.put("options", new String[]{
                        edit.boolType().trueText()
                        , edit.boolType().falseText()
                });
            } else {
                if (ChoiceType.Type.RADIO.equals(edit.choiceType().type())) {
                    json.put("expanding", true);//展开
                } else {
                    json.put("expanding", false);//不展开
                }
                VL[] vls = edit.choiceType().vl();
                if (vls == null || vls.length <= 0) {
                    throw new RuntimeException("A SelectInput must with options: " + fieldModel.getFieldName());
                }
                JSONArray ops = new JSONArray();
                for (VL vl : vls) {
                    ops.add(vl.label());
                }
                json.put("options", ops);
            }
        } else if ("MultipleSelect".equals(componentName)) {
            String[] vls = edit.tagsType().tags();
            if (vls == null || vls.length <= 0) {
                throw new RuntimeException("A MultipleSelect must with options: " + fieldModel.getFieldName());
            }
            json.put("options", vls);//这个可以被直接识别
        }
        return json;
    }

    private String repeat(String space, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) sb.append(space);
        return sb.toString();
    }

    public static EruptFormModel getEruptForm(String clazzName) {
        return ERUPT_FLOW_FORM_MAP.get(clazzName);
    }

    public static List<EruptFormModel> getEruptForms() {
        return ERUPT_FLOW_FORM_LIST;
    }
}
