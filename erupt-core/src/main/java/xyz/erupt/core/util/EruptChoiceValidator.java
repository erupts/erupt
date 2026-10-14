package xyz.erupt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Refuses a write whose CHOICE / MULTI_CHOICE value is not among the options the user is offered.
 * The page only lists valid options, but an API call does not go through the page.
 * <ul>
 *   <li>Only fields present in the submitted data are checked, so a cell edit checks its one field.</li>
 *   <li>On an edit a value equal to the stored one passes: an option retired since (a disabled
 *       department) must not block editing the rest of an old record.</li>
 *   <li>A disabled option is refused as a new value, the way the page offers it.</li>
 *   <li>Options are fetched once per field and linkage value within a request, so an import of many
 *       rows asks the handler once. Nothing outlives the request.</li>
 * </ul>
 * Off unless {@code erupt.choice-validate} is on; a field opts out with {@code validate = false}.
 *
 * @author YuePeng
 */
public final class EruptChoiceValidator {

    private static final String CACHE = EruptChoiceValidator.class.getName();

    private EruptChoiceValidator() {
    }

    /**
     * @param old the stored record on an edit, null on an add
     */
    public static void validate(EruptModel eruptModel, JsonObject data, Object old) {
        if (!EruptSpringUtil.getBean(EruptProp.class).isChoiceValidate()) return;
        Object form = null;
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            boolean multiple = edit.type() == EditType.MULTI_CHOICE;
            if (edit.type() != EditType.CHOICE && !multiple) continue;
            if (multiple ? !edit.multiChoiceType().validate() : !edit.choiceType().validate()) continue;
            JsonElement value = data.get(fieldModel.getFieldName());
            if (null == value || value.isJsonNull()) continue;
            List<String> submitted = new ArrayList<>();
            if (value.isJsonArray()) value.getAsJsonArray().forEach(it -> submitted.add(it.getAsString()));
            else submitted.add(value.getAsString());
            submitted.removeAll(stored(fieldModel, old));
            if (submitted.isEmpty()) continue;
            // the linkage handlers read the rest of the form, as the page's choice-item-filter call does
            if (null == form) form = GsonFactory.getGson().fromJson(data, eruptModel.getClazz());
            Set<String> options = options(eruptModel, fieldModel, form, multiple ? edit.multiChoiceType().dependField() : edit.choiceType().dependField(), data);
            for (String one : submitted) {
                if (!options.contains(one)) {
                    throw new EruptApiErrorTip(edit.title() + " " + I18nTranslate.$translate("erupt.choice.option_invalid") + " → " + one, R.PromptWay.MESSAGE);
                }
            }
        }
    }

    // The values the record already holds for the field, as strings
    private static Collection<String> stored(EruptFieldModel fieldModel, Object old) {
        if (null == old) return Collections.emptyList();
        try {
            Field field = fieldModel.getField();
            field.setAccessible(true);
            Object value = field.get(old);
            if (null == value) return Collections.emptyList();
            if (value instanceof Collection<?> collection) return collection.stream().map(EruptChoiceValidator::text).toList();
            return List.of(text(value));
        } catch (IllegalAccessException e) {
            return Collections.emptyList();
        }
    }

    private static String text(Object value) {
        return value instanceof Enum<?> e ? e.name() : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> options(EruptModel eruptModel, EruptFieldModel fieldModel, Object form, String dependField, JsonObject data) {
        Map<String, Set<String>> cache = (Map<String, Set<String>>) MetaContext.getVars().computeIfAbsent(CACHE, k -> new HashMap<>());
        String key = eruptModel.getEruptName() + "|" + fieldModel.getFieldName()
                + (dependField.isEmpty() ? "" : "|" + data.get(dependField));
        return cache.computeIfAbsent(key, k -> {
            Set<String> values = new HashSet<>();
            // a disabled option cannot be picked on the page either; a record already holding it passes as unchanged
            for (VLModel vl : EruptUtil.getChoiceListFilter(eruptModel, fieldModel, form)) if (!vl.isDisable()) values.add(vl.getValue());
            return values;
        });
    }

}
