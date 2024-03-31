package xyz.erupt.jpa.query;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2024/3/30 23:44
 */
@Getter
@Setter
public class QuerySchema {

    private Map<String, Object> params = new HashMap<>();

    private List<String> wheres = new ArrayList<>();

    private List<String> orders;

    private Integer limit;

    private Integer offset;

}
