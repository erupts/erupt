package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2020-02-13
 */
@Getter
@Setter
public class BiColumnVo {

    private int code;

    private String name;

    private Integer width;

    private Boolean sortable;

    private Boolean display;

    private Boolean drill;

    public BiColumnVo(String name, Integer width, Boolean sortable, Boolean display, Boolean drill) {
        this.code = name.hashCode();
        this.name = name;
        this.width = width;
        this.sortable = sortable;
        this.display = display == null || display;
        this.drill = drill;
    }
}
