package xyz.erupt.bi.cube.pojo.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/11/22 21:21
 */
@Getter
@Setter
public class CubeQuery {

    private String cube;

    private String explore;

    private List<String> dimensions;

    private List<String> measures;

    private List<CubeFilter> filters;

    private Map<String,Object> params;

    private boolean groupBy = true;

}
