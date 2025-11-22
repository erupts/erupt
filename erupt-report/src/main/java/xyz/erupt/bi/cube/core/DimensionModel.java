package xyz.erupt.bi.cube.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.cube.annotation.Dimension;

import java.lang.reflect.Field;

/**
 * @author YuePeng
 * date 2025/11/22 22:00
 */
@Getter
@Setter
public class DimensionModel {

    private Field field;

    private Dimension dimension;

}
