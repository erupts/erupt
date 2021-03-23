package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public EruptMenuVo() {
    }
}
