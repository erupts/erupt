package xyz.erupt.upms.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EruptMenuVo {

    private Long id;

    private String name;

    private String type;

    private String value;

    private String icon;

    private Long pid;

}
