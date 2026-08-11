package xyz.erupt.monitor.service;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.memory.service.EruptMemoryDataService;
import xyz.erupt.monitor.model.EruptFieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory data source for erupt field metadata; the drill scope condition is
 * parsed and applied by the memory base class.
 *
 * @author YuePeng
 */
@Service
public class EruptFieldInfoDataService extends EruptMemoryDataService<EruptFieldInfo> {

    public static final String DATA_PROCESSOR = "EruptFieldInfo";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptFieldInfoDataService.class);
    }

    @Override
    protected List<EruptFieldInfo> data(EruptModel eruptModel, EruptQuery eruptQuery) {
        List<EruptFieldInfo> list = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            for (EruptFieldModel fieldModel : model.getEruptFieldModels()) {
                list.add(this.toInfo(model, fieldModel));
            }
        }
        return list;
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

}
