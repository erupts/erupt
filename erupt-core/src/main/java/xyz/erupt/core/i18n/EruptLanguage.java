package xyz.erupt.core.i18n;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;
import java.util.stream.Stream;

/**
 * The languages erupt ships a translation column for — the single list behind the default of
 * {@code erupt-app.locales} and every language choice in a form. Each name is an English i18n key
 * (rows in erupt-core's CSV), so the list itself carries no translated text. Adding a language
 * means a new constant here and a new column in the CSV files.
 * <p>
 * Doubles as the {@link ChoiceFetchHandler} of a language field:
 * {@code @ChoiceType(fetchHandler = EruptLanguage.Choice.class)}.
 *
 * @author YuePeng
 * date 2026/11/20
 */
public enum EruptLanguage {

    EN_US("en-US", "English"),
    FR_FR("fr-FR", "French"),
    DE_DE("de-DE", "German"),
    ZH_CN("zh-CN", "Simplified Chinese"),
    ZH_TW("zh-TW", "Traditional Chinese"),
    JA_JP("ja-JP", "Japanese"),
    KO_KR("ko-KR", "Korean"),
    RU_RU("ru-RU", "Russian"),
    ES_ES("es-ES", "Spanish"),
    PT_PT("pt-PT", "Portuguese"),
    ID_ID("id-ID", "Indonesian"),
    AR_SA("ar-SA", "Arabic");

    private final String tag;

    private final String name;

    EruptLanguage(String tag, String name) {
        this.tag = tag;
        this.name = name;
    }

    /** The locale tag, e.g. zh-CN */
    public String tag() {
        return tag;
    }

    /** The English name, which is also its i18n key */
    public String displayName() {
        return name;
    }

    public static String[] tags() {
        return Stream.of(values()).map(EruptLanguage::tag).toArray(String[]::new);
    }

    /** Choice options: the tag as value, the translated name as label */
    public static class Choice implements ChoiceFetchHandler<Object> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Stream.of(values()).map(it -> new VLModel(it.tag, I18nTranslate.$translate(it.name))).toList();
        }

    }

}
