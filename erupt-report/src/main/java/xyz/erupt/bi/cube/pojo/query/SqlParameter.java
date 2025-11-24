package xyz.erupt.bi.cube.pojo.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SqlParameter {

    private String sql;

    private Map<String, Object> parameters;

}
