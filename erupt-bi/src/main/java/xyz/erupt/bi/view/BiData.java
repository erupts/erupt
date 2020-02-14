package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
@Getter
@Setter
public class BiData {

    Set<BiColumn> columns;

    List<Map<String, Object>> list;
}
