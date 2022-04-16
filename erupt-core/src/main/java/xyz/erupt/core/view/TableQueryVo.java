package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.query.Condition;

import java.util.List;

@Getter
@Setter
public class TableQueryVo {

    private static final int maxPageSize = 1000;

    private boolean dataExport = false;

    private Integer pageIndex;

    private Integer pageSize;

    private String sort;

    private Object linkTreeVal;

    private List<Condition> condition;

    public Integer getPageSize() {
        if (this.isDataExport()) {
            pageSize = Page.PAGE_MAX_DATA;
        } else {
            if (pageSize > maxPageSize) {
                pageSize = maxPageSize;
            }
        }
        return pageSize;
    }

}
