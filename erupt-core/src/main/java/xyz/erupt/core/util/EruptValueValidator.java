package xyz.erupt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Enforces on the server what an edit form declares, since an API call does not go through the
 * page: choice and tag values among their options, input length, number / slider / rate ranges,
 * date bounds, and the same for every row of a TAB_TABLE_ADD sub-table.
 * <ul>
 *   <li>Only fields present in the submitted data are checked, so a cell edit checks its one field.</li>
 *   <li>On an edit a value equal to the stored one passes: data saved before a rule tightened (a
 *       retired option, a lowered maximum) must not block editing the rest of an old record.</li>
 *   <li>Options are fetched once per field and linkage value within a request; nothing outlives it.</li>
 * </ul>
 * Choices are checked unless {@code erupt.choice-validate=false}, and a choice opts out with
 * {@code validate = false}; the declared ranges and lengths are always enforced.
 *
 * @author YuePeng
 */
public final class EruptValueValidator {

    private static final String CACHE = EruptValueValidator.class.getName();

    // Types with something to check; any other value (a MULTI_FORM block list, a reference object) is left alone
    private static final Set<EditType> CHECKED = EnumSet.of(EditType.CHOICE, EditType.MULTI_CHOICE, EditType.INPUT,
            EditType.NUMBER, EditType.SLIDER, EditType.RATE, EditType.TAGS, EditType.DATE);

    private EruptValueValidator() {
    }

    /**
     * @param old the stored record on an edit, null on an add
     */
    public static void validate(EruptModel eruptModel, JsonObject data, Object old) {
        boolean choices = EruptSpringUtil.getBean(EruptProp.class).isChoiceValidate();
        Object form = null;
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            Edit edit = fieldModel.getEruptField().edit();
            JsonElement value = data.get(fieldModel.getFieldName());
            if (null == value || value.isJsonNull()) continue;
            if (edit.type() == EditType.TAB_TABLE_ADD) {
                rows(fieldModel, value, old);
                continue;
            }
            if (!CHECKED.contains(edit.type())) continue;
            List<String> submitted = values(edit, value);
            submitted.removeAll(stored(fieldModel, old));
            if (submitted.isEmpty()) continue;
            switch (edit.type()) {
                case CHOICE, MULTI_CHOICE -> {
                    boolean multiple = edit.type() == EditType.MULTI_CHOICE;
                    if (!choices || !(multiple ? edit.multiChoiceType().validate() : edit.choiceType().validate())) continue;
                    // a choice that declares no options at all (no vl, no handler, not an enum) has nothing to check against
                    if (!declaresOptions(fieldModel, edit, multiple)) continue;
                    // the linkage handlers read the rest of the form, as the page's choice-item-filter call does
                    if (null == form) form = GsonFactory.getGson().fromJson(data, eruptModel.getClazz());
                    Set<String> options = options(eruptModel, fieldModel, form,
                            multiple ? edit.multiChoiceType().dependField() : edit.choiceType().dependField(), data);
                    for (String one : submitted) {
                        if (!options.contains(one)) throw refuse(edit, "erupt.choice.option_invalid", one);
                    }
                }
                case INPUT -> {
                    for (String one : submitted) {
                        if (one.length() > edit.inputType().length()) throw refuse(edit, "erupt.data.limit_length", one.length() + " > " + edit.inputType().length());
                    }
                }
                case NUMBER -> {
                    // the declared defaults are the int range, which a long column may rightly exceed: bound only what was set
                    NumberType type = edit.numberType();
                    Long min = type.min() == -Integer.MAX_VALUE ? null : type.min();
                    Long max = type.max() == Integer.MAX_VALUE ? null : type.max();
                    for (String one : submitted) range(edit, one, null == min ? null : BigDecimal.valueOf(min), null == max ? null : BigDecimal.valueOf(max));
                }
                case SLIDER -> {
                    for (String one : submitted) range(edit, one, BigDecimal.valueOf(edit.sliderType().min()), BigDecimal.valueOf(edit.sliderType().max()));
                }
                case RATE -> {
                    for (String one : submitted) {
                        BigDecimal rate = range(edit, one, BigDecimal.ZERO, BigDecimal.valueOf(edit.rateType().count()));
                        BigDecimal step = edit.rateType().allowHalf() ? new BigDecimal("0.5") : BigDecimal.ONE;
                        if (rate.remainder(step).signum() != 0) throw refuse(edit, "erupt.value.out_of_range", one);
                    }
                }
                case TAGS -> {
                    List<String> all = values(edit, value);
                    if (all.size() > edit.tagsType().maxTagCount()) throw refuse(edit, "erupt.value.out_of_range", all.size() + " > " + edit.tagsType().maxTagCount());
                    if (edit.tagsType().allowExtension()) continue;
                    if (null == form) form = GsonFactory.getGson().fromJson(data, eruptModel.getClazz());
                    Set<String> tags = tags(eruptModel, fieldModel, form);
                    for (String one : submitted) {
                        if (!tags.contains(one)) throw refuse(edit, "erupt.choice.option_invalid", one);
                    }
                }
                case DATE -> {
                    for (String one : submitted) date(edit, one);
                }
                default -> {
                }
            }
        }
    }

    // ------------------------------------------------------------------ sub-table rows

    // Each row is checked like a record of the sub model: its own rules, then the ones above against the stored row
    private static void rows(EruptFieldModel fieldModel, JsonElement value, Object old) {
        if (!value.isJsonArray()) return;
        EruptModel subModel = EruptCoreService.getErupt(fieldModel.getFieldReturnName());
        if (null == subModel) return;
        String pk = subModel.getErupt().primaryKeyCol();
        Map<String, Object> stored = new HashMap<>();
        for (Object row : storedCollection(fieldModel, old)) {
            Object id = read(subModel, row, pk);
            if (null != id) stored.put(String.valueOf(id), row);
        }
        JsonArray rows = value.getAsJsonArray();
        for (int i = 0; i < rows.size(); i++) {
            if (!rows.get(i).isJsonObject()) continue;
            JsonObject row = rows.get(i).getAsJsonObject();
            String prefix = fieldModel.getEruptField().edit().title() + " #" + (i + 1) + ": ";
            R<Void> result = EruptUtil.validateEruptValue(subModel, row);
            if (!result.isSuccess()) throw new EruptApiErrorTip(prefix + result.getMessage(), R.PromptWay.MESSAGE);
            JsonElement id = row.get(pk);
            Object storedRow = null == id || id.isJsonNull() ? null : stored.get(id.getAsString());
            try {
                validate(subModel, row, storedRow);
            } catch (EruptApiErrorTip e) {
                throw new EruptApiErrorTip(prefix + e.getMessage(), R.PromptWay.MESSAGE);
            }
        }
    }

    private static Collection<?> storedCollection(EruptFieldModel fieldModel, Object old) {
        if (null == old) return Collections.emptyList();
        Object value = read(fieldModel.getField(), old);
        return value instanceof Collection<?> collection ? collection : Collections.emptyList();
    }

    // ------------------------------------------------------------------ values

    // The submitted value as strings: array elements, a tag list split by its separator, else the one value
    private static List<String> values(Edit edit, JsonElement value) {
        List<String> values = new ArrayList<>();
        if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(it -> values.add(it.getAsString()));
        } else if (edit.type() == EditType.TAGS) {
            String text = value.getAsString();
            if (text.isEmpty()) return values;
            String separator = edit.tagsType().joinSeparator();
            if ("[]".equals(separator)) {
                try {
                    JsonParser.parseString(text).getAsJsonArray().forEach(it -> values.add(it.getAsString()));
                } catch (RuntimeException e) {
                    values.add(text);
                }
            } else {
                values.addAll(Arrays.asList(text.split(java.util.regex.Pattern.quote(separator))));
            }
        } else if (value.isJsonPrimitive()) {
            values.add(value.getAsString());
        }
        return values;
    }

    // The values the record already holds for the field, as strings
    private static Collection<String> stored(EruptFieldModel fieldModel, Object old) {
        if (null == old) return Collections.emptyList();
        Object value = read(fieldModel.getField(), old);
        if (null == value) return Collections.emptyList();
        if (value instanceof Collection<?> collection) return collection.stream().map(EruptValueValidator::text).toList();
        Edit edit = fieldModel.getEruptField().edit();
        if (edit.type() == EditType.TAGS) return values(edit, GsonFactory.getGson().toJsonTree(value.toString()));
        return List.of(text(value));
    }

    private static String text(Object value) {
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof Number n) return new BigDecimal(n.toString()).stripTrailingZeros().toPlainString();
        return String.valueOf(value);
    }

    private static Object read(EruptModel model, Object bean, String fieldName) {
        EruptFieldModel fieldModel = model.getEruptFieldMap().get(fieldName);
        return null == fieldModel ? null : read(fieldModel.getField(), bean);
    }

    private static Object read(Field field, Object bean) {
        if (null == field || null == bean) return null;
        try {
            field.setAccessible(true);
            return field.get(bean);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ checks

    private static BigDecimal range(Edit edit, String value, BigDecimal min, BigDecimal max) {
        BigDecimal number;
        try {
            number = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw refuse(edit, "erupt.must.number", value);
        }
        if ((null != min && number.compareTo(min) < 0) || (null != max && number.compareTo(max) > 0)) {
            throw refuse(edit, "erupt.value.out_of_range", value);
        }
        return number;
    }

    // Bounds apply to the day: date and date-time values; time, month, week, quarter and year pickers are left as they are
    private static void date(Edit edit, String value) {
        DateType type = edit.dateType();
        if (type.type() != DateType.Type.AUTO && type.type() != DateType.Type.DATE && type.type() != DateType.Type.DATE_TIME) return;
        if (type.pickerMode() == DateType.PickerMode.ALL && type.min().isEmpty() && type.max().isEmpty()) return;
        LocalDate day;
        try {
            day = LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
        } catch (DateTimeParseException e) {
            return;
        }
        LocalDate today = LocalDate.now();
        boolean out = (type.pickerMode() == DateType.PickerMode.FUTURE && day.isBefore(today))
                || (type.pickerMode() == DateType.PickerMode.HISTORY && day.isAfter(today))
                || (!type.min().isEmpty() && day.isBefore(LocalDate.parse(type.min())))
                || (!type.max().isEmpty() && day.isAfter(LocalDate.parse(type.max())));
        if (out) throw refuse(edit, "erupt.value.out_of_range", value);
    }

    private static EruptApiErrorTip refuse(Edit edit, String key, String detail) {
        return new EruptApiErrorTip(edit.title() + " " + I18nTranslate.$translate(key) + " → " + detail, R.PromptWay.MESSAGE);
    }

    // ------------------------------------------------------------------ options

    // A handler always counts, even one that offers this user nothing. vl counts only with a real value:
    // designer-built models proxy their annotations from a template whose vl holds one blank placeholder.
    private static boolean declaresOptions(EruptFieldModel fieldModel, Edit edit, boolean multiple) {
        if ((multiple ? edit.multiChoiceType().fetchHandler() : edit.choiceType().fetchHandler()).length > 0) return true;
        for (VL vl : multiple ? edit.multiChoiceType().vl() : edit.choiceType().vl()) {
            if (!vl.value().isEmpty()) return true;
        }
        Field field = fieldModel.getField();
        if (null == field) return false;
        Class<?> type = field.getType();
        if (Collection.class.isAssignableFrom(type)) type = ReflectUtil.fieldGenericClass(field);
        return null != type && type.isEnum();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> cache() {
        return (Map<String, Set<String>>) MetaContext.getVars().computeIfAbsent(CACHE, k -> new HashMap<>());
    }

    private static Set<String> options(EruptModel eruptModel, EruptFieldModel fieldModel, Object form, String dependField, JsonObject data) {
        String key = eruptModel.getEruptName() + "|" + fieldModel.getFieldName()
                + (dependField.isEmpty() ? "" : "|" + data.get(dependField));
        return cache().computeIfAbsent(key, k -> {
            Set<String> values = new HashSet<>();
            // a disabled option cannot be picked on the page either; a record already holding it passes as unchanged
            for (VLModel vl : EruptUtil.getChoiceListFilter(eruptModel, fieldModel, form)) if (!vl.isDisable()) values.add(vl.getValue());
            return values;
        });
    }

    @SuppressWarnings("unchecked")
    private static Set<String> tags(EruptModel eruptModel, EruptFieldModel fieldModel, Object form) {
        Edit edit = fieldModel.getEruptField().edit();
        return cache().computeIfAbsent(eruptModel.getEruptName() + "|" + fieldModel.getFieldName(), k -> {
            Set<String> values = new HashSet<>(Arrays.asList(edit.tagsType().tags()));
            for (Class<? extends TagsFetchHandler> handler : edit.tagsType().fetchHandler()) {
                Optional.ofNullable(EruptSpringUtil.getBean(handler).fetchTags(form, edit.tagsType().fetchHandlerParams())).ifPresent(values::addAll);
            }
            return values;
        });
    }

}
