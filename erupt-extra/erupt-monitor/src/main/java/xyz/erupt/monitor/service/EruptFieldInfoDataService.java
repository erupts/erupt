package xyz.erupt.monitor.service;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.monitor.model.EruptFieldInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-memory data source for erupt field metadata; the drill scope arrives as an
 * HQL-style condition string (see EruptService.drillProcess) and is parsed here.
 *
 * @author YuePeng
 */
@Service
public class EruptFieldInfoDataService extends MemoryDataService {

    public static final String DATA_PROCESSOR = "EruptFieldInfo";

    private static final Pattern DRILL_CONDITION = Pattern.compile("^\\s*EruptFieldInfo\\.eruptName\\s*=\\s*'(.*)'\\s*$");

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptFieldInfoDataService.class);
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        String[] split = id.toString().split("\\.", 2);
        if (split.length != 2) return null;
        EruptModel target = EruptCoreService.getErupt(split[0]);
        if (null == target) return null;
        EruptFieldModel fieldModel = target.getEruptFieldMap().get(split[1]);
        return null == fieldModel ? null : this.toInfo(target, fieldModel);
    }

    @Override
    protected List<Map<String, Object>> rows(EruptQuery eruptQuery) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (EruptModel model : this.scope(eruptQuery)) {
            for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
                rows.add(this.toMap(this.toInfo(model, fieldModel)));
            }
        }
        return rows;
    }

    private List<EruptModel> scope(EruptQuery eruptQuery) {
        if (null != eruptQuery.getConditionStrings()) {
            for (String conditionString : eruptQuery.getConditionStrings()) {
                Matcher matcher = DRILL_CONDITION.matcher(conditionString);
                if (matcher.matches()) {
                    EruptModel model = EruptCoreService.getErupt(matcher.group(1));
                    return null == model ? Collections.emptyList() : Collections.singletonList(model);
                }
            }
        }
        return EruptCoreService.getErupts();
    }

    private EruptFieldInfo toInfo(EruptModel model, EruptFieldModel fieldModel) {
        // prefer the raw annotation (bypasses the i18n/expr proxy so source values are shown);
        // runtime-registered erupts have no reflective annotation, so fall back to the proxied one
        EruptField eruptField = Optional.ofNullable(fieldModel.getField().getAnnotation(EruptField.class))
                .orElse(fieldModel.getEruptField());
        EruptFieldInfo info = new EruptFieldInfo();
        info.setId(model.getEruptName() + "." + fieldModel.getFieldName());
        info.setEruptName(model.getEruptName());
        info.setFieldName(fieldModel.getFieldName());
        info.setTitle(eruptField.edit().title().isEmpty()
                ? (eruptField.views().length > 0 ? eruptField.views()[0].title() : null)
                : eruptField.edit().title());
        info.setFieldType(fieldModel.getField().getType().getSimpleName());
        info.setEditType(eruptField.edit().type().name());
        info.setNotNull(eruptField.edit().notNull());
        info.setSearchable(eruptField.edit().search().value());
        return info;
    }

    private Map<String, Object> toMap(EruptFieldInfo info) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(LambdaSee.field(EruptFieldInfo::getId), info.getId());
        map.put(LambdaSee.field(EruptFieldInfo::getEruptName), info.getEruptName());
        map.put(LambdaSee.field(EruptFieldInfo::getFieldName), info.getFieldName());
        map.put(LambdaSee.field(EruptFieldInfo::getTitle), info.getTitle());
        map.put(LambdaSee.field(EruptFieldInfo::getFieldType), info.getFieldType());
        map.put(LambdaSee.field(EruptFieldInfo::getEditType), info.getEditType());
        map.put(LambdaSee.field(EruptFieldInfo::getNotNull), info.getNotNull());
        map.put(LambdaSee.field(EruptFieldInfo::getSearchable), info.getSearchable());
        return map;
    }

}
