package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/6/13 22:42
 */
@Getter
@Setter
public class BiChartVo {

    private Long id;

    private String code;

    private String name;

    private Integer grid;

    private Integer height;

    private Integer sort;

    private String type;

    private String chartOption;

    private String remark;

}
