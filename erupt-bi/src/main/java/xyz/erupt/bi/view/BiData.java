package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.core.view.Page;

import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-02-13
 */
@Getter
@Setter
public class BiData {

    String code;

    String name;

    List<BiColumn> columns;

    List<Map<String, Object>> list;

    Page page;
}
