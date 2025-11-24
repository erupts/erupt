package xyz.erupt.bi.cube.pojo.core;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.bi.cube.annotation.Measure;

import java.lang.reflect.Field;

/**
 * @author YuePeng
 * date 2025/11/22 22:00
 */
@Getter
@Setter
public class MeasureModel {

    private Field field;

    private Measure measure;

}
