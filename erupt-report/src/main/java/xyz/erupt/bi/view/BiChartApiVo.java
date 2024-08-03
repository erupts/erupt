package xyz.erupt.bi.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2024/8/3 20:35
 */
@Getter
@Setter
public class BiChartApiVo {

    private List<Map<String, Object>> data;

    private List<Column> columns;

    @Getter
    @Setter
    public static class Column {

        private String name;

    }

}
