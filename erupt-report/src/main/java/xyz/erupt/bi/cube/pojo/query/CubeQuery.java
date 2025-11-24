package xyz.erupt.bi.cube.pojo.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YuePeng
 * date 2025/11/22 21:21
 */
@Getter
@Setter
public class CubeQuery {

    private String cubeName;

    private List<String> dimensions;

    private List<String> measures;

    private List<CubeFilter> filters;

    private boolean groupBy = true;

}
