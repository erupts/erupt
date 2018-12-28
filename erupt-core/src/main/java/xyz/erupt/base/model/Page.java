package xyz.erupt.base.model;

import lombok.Data;

import java.util.List;

/**
 * Created by liyuepeng on 10/12/18.
 */
@Data
public class Page {
    private int pageIndex = 1;

    private int pageSize = 20;

    private long total;

    private String sort;

    private List list;

    public static final String PAGE_NUMBER_STR = "pageIndex";

    public static final String PAGE_SIZE_STR = "pageSize";

    public Page(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public Page() {

    }
}
