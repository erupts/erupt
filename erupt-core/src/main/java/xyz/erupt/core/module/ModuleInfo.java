package xyz.erupt.core.module;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2022/1/4 21:13
 */
@Getter
@Setter
@Builder
public class ModuleInfo {

    // the language placeholder is filled in by the frontend with the reader's language
    private static final String ERUPT_DOCS = "https://docs.erupt.xyz/{lang}/";

    private String name;

    // one sentence on what the module solves, an i18n key as every user-visible text
    private String description;

    // null for a module from outside erupt that does not say; the module center then lists it under "other"
    private ModuleGroup group;

    // the module's documentation; see eruptDocs(String) for one on docs.erupt.xyz
    private String docs;

    /** The URL of a page on docs.erupt.xyz, such as {@code modules/erupt-job}. */
    public static String eruptDocs(String page) {
        return ERUPT_DOCS + page;
    }

}
