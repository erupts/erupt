package xyz.erupt.report.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2020-02-13
 */
@Getter
@Setter
public class BiVo {

    private Long id;

    private String code;

    private String pageType;

    private String remark; // report description

    private boolean export;

    private boolean table;

    private Integer refreshTime;

    private List<BiDimensionVo> dimensions;

    private List<BiChartVo> charts;

    private Integer[] pageSizeOptions;

    private Integer pageSize;

}
