package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * One card of the module center.
 *
 * @author YuePeng
 * date 2026-11-01
 */
@Getter
@Setter
public class ModuleVo {

    private String name;

    /** Translated group title, ready to display; the list arrives sorted by it */
    private String group;

    /** Translated for the current session */
    private String description;

    /** Documentation URL, may hold a {lang} placeholder for the reader's language */
    private String docs;

    /** Menus the module declares, buttons left out; null when it declares none */
    private Integer menus;

    /** Whether its menus are switched on; null when the module cannot be switched off */
    private Boolean enabled;

}
