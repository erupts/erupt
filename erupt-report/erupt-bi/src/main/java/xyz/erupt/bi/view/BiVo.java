package xyz.erupt.bi.view;

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

    private boolean export;

    private boolean table;

    private int refreshTime;

    private List<BiDimensionVo> dimensions;

    private List<BiChartVo> charts;

}
