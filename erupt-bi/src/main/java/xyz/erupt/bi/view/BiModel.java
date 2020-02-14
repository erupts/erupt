package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiDimension;

import java.util.Set;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
@Getter
@Setter
public class BiModel {

    private Boolean export;

    private Set<BiDimension> dimensions;

    private Set<BiChart> charts;
}
