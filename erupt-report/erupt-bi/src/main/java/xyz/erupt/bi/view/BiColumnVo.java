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

    private String name;

    private Integer width;

    private Boolean sortable;

    private Boolean display;

    public BiColumnVo(String name, Integer width, Boolean sortable, Boolean display) {
        this.name = name;
        this.width = width;
        this.sortable = sortable;
        this.display = display == null || display;
    }
}
