package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/6/13 22:35
 */
@Getter
@Setter
public class BiDimensionVo {

    private Long id;

    private String code;

    private String title;

    private Integer sort;

    private Boolean notNull;

    private Object defaultValue;

    private String type;

}
