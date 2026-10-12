package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.core.module.MetaMenu;

@Getter
@Setter
@NoArgsConstructor
public class EruptMenuVo {

    /** Menu id */
    private Long id;

    /** Menu code */
    private String code;

    /** Menu text */
    private String name;

    /** Menu type: erupt table, tpl page, link and so on */
    private String type;

    /** Menu value, meaning depends on type, such as the erupt class name */
    private String value;

    /** Icon name */
    private String icon;

    /** Parent menu id */
    private Long pid;

    public EruptMenuVo(Long id, String code, String name, String type, String value, String icon, Long pid) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.value = value;
        this.icon = icon;
        this.pid = pid;
    }

    public static EruptMenuVo fromMetaMenu(MetaMenu metaMenu) {
        return new EruptMenuVo(metaMenu.getId(), metaMenu.getCode(), metaMenu.getName(), metaMenu.getType(), metaMenu.getValue(),
                metaMenu.getIcon(), metaMenu.getParentMenu() == null ? null : metaMenu.getParentMenu().getId());
    }

}
