package xyz.erupt.test.core;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import xyz.erupt.core.i18n.I18nRunner;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.test.EruptApplicationTests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the i18n subsystem: I18nRunner (CSV loading + lookup) and I18nTranslate (Spring service).
 */
public class I18nTest extends EruptApplicationTests {

    @Resource
    private I18nTranslate i18nTranslate;

    @Resource
    private EruptProp eruptProp;

    // ─── I18nRunner — static CSV loading ─────────────────────────────────────

    /** All 12 language codes declared in the CSV header must be present after startup. */
    @Test
    void testExpectedLanguagesLoaded() {
        List<String> langs = I18nRunner.langs();
        assertFalse(langs.isEmpty(), "langs() must not be empty — CSV files were not loaded");
        for (String lang : List.of("zh-CN", "zh-TW", "en-US", "fr-FR", "ja-JP",
                "ko-KR", "ru-RU", "es-ES", "de-DE", "pt-PT", "id-ID", "ar-SA")) {
            assertTrue(langs.contains(lang), "Expected language '" + lang + "' not found in langs()");
        }
    }

    /** Known key "Y" must translate correctly in Chinese and English. */
    @Test
    void testKnownKeyTranslation() {
        assertEquals("是", I18nRunner.getI18nValue("zh-CN", "Y"), "zh-CN 'Y' must be '是'");
        assertEquals("yes", I18nRunner.getI18nValue("en-US", "Y"), "en-US 'Y' must be 'yes'");
        assertEquals("否", I18nRunner.getI18nValue("zh-CN", "N"), "zh-CN 'N' must be '否'");
        assertEquals("no", I18nRunner.getI18nValue("en-US", "N"), "en-US 'N' must be 'no'");
    }

    /** Unknown key falls back to the key string itself. */
    @Test
    void testMissingKeyFallbackToKey() {
        String key = "i18n.test.nonexistent.key.xyz";
        assertEquals(key, I18nRunner.getI18nValue("zh-CN", key));
        assertEquals(key, I18nRunner.getI18nValue("en-US", key));
    }

    /** Unknown language falls back to the key string itself. */
    @Test
    void testMissingLangFallbackToKey() {
        assertEquals("Y", I18nRunner.getI18nValue("xx-XX", "Y"),
                "Unknown lang must return the key");
    }

    /** Language lookup is case-insensitive (I18nRunner extends LinkedCaseInsensitiveMap). */
    @Test
    void testCaseInsensitiveLangLookup() {
        String upper = I18nRunner.getI18nValue("zh-CN", "Y");
        String lower = I18nRunner.getI18nValue("zh-cn", "Y");
        assertEquals(upper, lower, "Lang lookup must be case-insensitive");

        assertEquals("yes", I18nRunner.getI18nValue("EN-US", "Y"),
                "EN-US must resolve same as en-US");
    }

    /**
     * CSV quote parsing: key "N" in id-ID is stored as "Tidak," (with comma) in the CSV.
     * Verifies the parser correctly strips surrounding quotes and preserves the inner comma.
     */
    @Test
    void testCsvQuotedValueWithComma() {
        String value = I18nRunner.getI18nValue("id-ID", "N");
        assertTrue(value.contains(","),
                "id-ID 'N' must contain a comma — CSV quoted-value parsing may be broken");
        assertFalse(value.startsWith("\""),
                "id-ID 'N' must not start with a quote — surrounding quotes must be stripped");
    }

    /** Keys from erupt-upms CSV are loaded alongside erupt-core keys (cross-module loading). */
    @Test
    void testCrossModuleKeysLoaded() {
        String value = I18nRunner.getI18nValue("zh-CN", "upms.pwd_required");
        assertNotEquals("upms.pwd_required", value,
                "upms.pwd_required must be translated, not returned as-is");
        assertEquals("密码必填", value, "zh-CN upms.pwd_required must be '密码必填'");
    }

    /** Every real language (excluding the "key" pseudo-entry) must translate key "Y". */
    @Test
    void testAllLangMapsNonEmpty() {
        for (String lang : I18nRunner.langs()) {
            if ("key".equalsIgnoreCase(lang)) continue; // first CSV column header, not a language
            String result = I18nRunner.getI18nValue(lang, "Y");
            assertNotEquals("Y", result,
                    "Lang '" + lang + "' must translate key 'Y' (map may be empty)");
        }
    }

    // ─── I18nTranslate — Spring service ──────────────────────────────────────

    /** translate(lang, key) uses explicit lang, ignoring request header. */
    @Test
    void testTranslateWithExplicitLang() {
        assertEquals("是", i18nTranslate.translate("zh-CN", "Y"));
        assertEquals("yes", i18nTranslate.translate("en-US", "Y"));
    }

    /** translate(key) with no active HTTP request falls back to eruptProp.defaultLocales. */
    @Test
    void testTranslateDefaultLocale() {
        String defaultLang = eruptProp.getDefaultLocales();
        String expected = I18nRunner.getI18nValue(defaultLang, "Y");
        assertEquals(expected, i18nTranslate.translate("Y"),
                "translate(key) must use defaultLocales when no Lang header is present");
    }

    /** getLang() must not throw when called outside of an HTTP request context. */
    @Test
    void testGetLangOutsideRequestContextReturnsNull() {
        assertDoesNotThrow(() -> i18nTranslate.getLang(),
                "getLang() must not throw outside servlet context");
        assertNull(i18nTranslate.getLang(),
                "getLang() must return null when no Lang header is present");
    }

    /** $translate() static convenience method works in Spring context. */
    @Test
    void testStaticTranslate() {
        String value = I18nTranslate.$translate("Y");
        assertNotNull(value, "$translate must not return null");
        assertFalse(value.isBlank(), "$translate must not return blank");
    }

    /** translate() gracefully handles null-like unknown keys (returns the key). */
    @Test
    void testTranslateUnknownKeyReturnsKey() {
        String key = "i18n.nonexistent.zzz";
        assertEquals(key, i18nTranslate.translate("en-US", key));
    }
}
