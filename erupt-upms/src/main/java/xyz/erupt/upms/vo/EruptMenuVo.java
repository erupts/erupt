package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.core.module.MetaMenu;

@Getter
@Setter
@NoArgsConstructor
public class EruptMenuVo {

    private Long id;

    private String code;

    private String name;

    private String type;

    private String value;

    private String icon;

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
