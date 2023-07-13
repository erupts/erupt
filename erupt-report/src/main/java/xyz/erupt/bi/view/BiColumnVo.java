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

    private String type;

    private Boolean drill;

    //表格列备注
    private String remark;

    public BiColumnVo(String name, Integer width, Boolean sortable, Boolean display, String type, String remark) {
        this.code = name.hashCode();
        this.name = name;
        this.width = width;
        this.sortable = sortable;
        this.display = display == null || display;
        this.type = type;
        this.remark = remark;
    }
}
