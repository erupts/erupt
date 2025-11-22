package xyz.erupt.bi.cube.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.cube.annotation.Cube;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/11/22 21:25
 */
@Getter
@Setter
public class CubeModel {

    private Class<?> clazz;

    private Cube cube;

    private Map<String, DimensionModel> dimensionMap = new HashMap<>();

    private Map<String, MeasureModel> measureMap = new HashMap<>();

}
