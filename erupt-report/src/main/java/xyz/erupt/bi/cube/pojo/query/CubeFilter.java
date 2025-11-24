package xyz.erupt.bi.cube.pojo.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CubeFilter {

    private String field;

    private String operator;

    private Object value;

}
