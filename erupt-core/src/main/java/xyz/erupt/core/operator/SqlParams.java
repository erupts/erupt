package xyz.erupt.core.operator;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2025/9/15 22:30
 */
@Getter
@Setter
public class SqlParams {

    private String sql;

    private Map<String, Object> params = new HashMap<>();

}
