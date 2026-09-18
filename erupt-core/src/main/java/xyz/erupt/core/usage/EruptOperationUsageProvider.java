package xyz.erupt.core.usage;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A row operation may open a model of its own as the button's form. The button says which model it
 * opens; the model says nothing about being opened, and a form model exists for no other reason —
 * which is why one that lost its button looks like a model that was simply forgotten.
 *
 * @author YuePeng
 */
@Service
public class EruptOperationUsageProvider implements EruptUsageProvider {

    private static final String OWNER_SEPARATOR = " · ";

    @Override
    public List<EruptUsage> usages() {
        String usage = I18nTranslate.$translate("erupt.usage.row_operation");
        List<EruptUsage> usages = new ArrayList<>();
        for (EruptModel model : EruptCoreService.getErupts()) {
            String owner = i18n(model.getClazz(), model.getErupt().name());
            for (RowOperation operation : model.getErupt().rowOperation()) {
                if (void.class == operation.eruptClass()) continue;
                usages.add(new EruptUsage(operation.eruptClass(), usage,
                        owner + OWNER_SEPARATOR + i18n(model.getClazz(), operation.title()),
                        EruptUsage.tableRoute(model.getClazz())));
            }
        }
        return usages;
    }

    // Annotation text follows the framework rule: a class opts into translation with @EruptI18n,
    // so a user model named the same as a framework key is left alone
    private static String i18n(Class<?> clazz, String text) {
        if (null == text || text.isEmpty() || null == clazz.getAnnotation(EruptI18n.class)) return text;
        return I18nTranslate.$translate(text);
    }

}
