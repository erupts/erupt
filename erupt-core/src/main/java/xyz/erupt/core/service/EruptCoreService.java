package xyz.erupt.core.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.exception.EruptAnnotationException;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.toolkit.TimeRecorder;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * @author YuePeng
 * date 9/28/18.
 */
@Order
@Service
@Slf4j
public class EruptCoreService implements ApplicationRunner {

    private static final Map<String, EruptModel> ERUPTS = new LinkedCaseInsensitiveMap<>();

    public static EruptModel getErupt(String eruptName) {
        return ERUPTS.get(eruptName);
    }

    //需要动态构建的EruptModel视图属性
    @SneakyThrows
    public static EruptModel getEruptView(String eruptName) {
        EruptModel em = getErupt(eruptName).clone();
        for (EruptFieldModel fieldModel : em.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            if (edit.type() == EditType.CHOICE) {
                fieldModel.setChoiceList(EruptUtil.getChoiceList(edit.choiceType()));
            } else if (edit.type() == EditType.TAGS) {
                fieldModel.setTagList(EruptUtil.getTagList(edit.tagsType()));
            }
        }
        if (em.getErupt().rowOperation().length > 0) {
            boolean copy = false;
            for (RowOperation rowOperation : em.getErupt().rowOperation()) {
                if (!ExprInvoke.getExpr(rowOperation.show())) {
                    if (!copy) {
                        copy = true;
                        em.setEruptJson(em.getEruptJson().deepCopy());
                    }
                    JsonArray jsonArray = em.getEruptJson().getAsJsonArray("rowOperation");
                    for (JsonElement operation : jsonArray) {
                        if (rowOperation.code().equals(operation.getAsJsonObject().get("code").getAsString())) {
                            jsonArray.remove(operation);
                            break;
                        }
                    }
                }
            }
        }
        return em;
    }

    private static EruptModel initEruptModel(Class<?> clazz) {
        //erupt class data to memory
        EruptModel eruptModel = new EruptModel(clazz);
        // erupt field data to memory
        eruptModel.setEruptFieldModels(new ArrayList<>());
        eruptModel.setEruptFieldMap(new LinkedCaseInsensitiveMap<>());
        ReflectUtil.findClassAllFields(clazz, field -> Optional.ofNullable(field.getAnnotation(EruptField.class)).ifPresent(ignore -> {
            EruptFieldModel eruptFieldModel = new EruptFieldModel(field);
            eruptModel.getEruptFieldModels().add(eruptFieldModel);
            eruptModel.getEruptFieldMap().put(field.getName(), eruptFieldModel);
        }));
        eruptModel.getEruptFieldModels().sort(Comparator.comparingInt((a) -> a.getEruptField().sort()));
        // erupt annotation validate
        EruptAnnotationException.validateEruptInfo(eruptModel);
        return eruptModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        TimeRecorder timeRecorder = new TimeRecorder();
        EruptSpringUtil.scannerPackage(EruptApplication.getScanPackage(), new TypeFilter[]{
                new AnnotationTypeFilter(Erupt.class)
        }, clazz -> ERUPTS.put(clazz.getSimpleName(), initEruptModel(clazz)));
        log.info("Erupt core initialization completed in {} ms", timeRecorder.recorder());
    }
}
