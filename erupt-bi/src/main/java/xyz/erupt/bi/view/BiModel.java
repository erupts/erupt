package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiDimension;

import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
@Getter
@Setter
public class BiModel {

    private Boolean export;

    private List<BiDimension> dimensions;

    private List<BiChart> charts;
}
