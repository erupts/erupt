package xyz.erupt.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by liyuepeng on 10/12/18.
 */
@Data
public class Page {
    private int pageIndex = 1;

    private int pageSize = 10;

    private int totalPage;

    private Long total;

    private String sort;

    private List list;

    public static final String PAGE_INDEX_STR = "pageIndex";

    public static final String PAGE_SIZE_STR = "pageSize";

    public static final String PAGE_SORT_STR = "sort";

    public Page(int pageIndex, int pageSize, String sort) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.sort = sort;
        //防止单页数据过大的情况
        if (pageSize > 1000) {
            this.pageSize = 1000;
        }
    }

    public Page() {

    }

    public void setTotal(Long total) {
        this.total = total;
        if (total % pageSize == 0) {
            totalPage = total.intValue() / pageSize;
        } else {
            totalPage = total.intValue() / pageSize + 1;
        }
    }
}
