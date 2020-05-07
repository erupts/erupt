package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
@Getter
@Setter
public class BiData {

    private List<BiColumn> columns;

    private List<Map<String, Object>> list;

    private Long total;
}
