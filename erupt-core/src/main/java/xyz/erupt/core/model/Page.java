package xyz.erupt.core.model;

import lombok.Data;

import java.util.List;

/**
 * Created by liyuepeng on 10/12/18.
 */
@Data
public class Page {
    private int pageIndex;

    private int pageSize;

    private int totalPage;

    private Long total;

    private String sort;

    private List list;

    public static final String PAGE_INDEX_STR = "_pageIndex";

    public static final String PAGE_SIZE_STR = "_pageSize";

    public static final String PAGE_SORT_STR = "_sort";

    public Page(int pageIndex, int pageSize, String sort) {
        this.pageIndex = pageIndex == 0 ? 1 : pageIndex;
        this.pageSize = pageSize;
        this.sort = sort;
        //单页最大数据量10000
        if (pageSize > 10000) {
            this.pageSize = 10000;
        }
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
