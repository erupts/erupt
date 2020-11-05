package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2018-10-12.
 */
@Getter
@Setter
public class Page {
    private int pageIndex;

    private int pageSize;

    private int totalPage;

    private Long total;

    private String sort;

    public static final int PAGE_MAX_DATA = 1000000;

    private Collection<Map<String, Object>> list;

    public Page(int pageIndex, int pageSize, String sort) {
        this.pageIndex = pageIndex == 0 ? 1 : pageIndex;
        this.pageSize = pageSize;
        this.sort = sort;
        //支持最大数据量100万
        if (pageSize > PAGE_MAX_DATA) {
            this.pageSize = PAGE_MAX_DATA;
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
