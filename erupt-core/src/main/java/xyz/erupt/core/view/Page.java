package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.config.Comment;

import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-12.
 */
@Getter
@Setter
public class Page {
    private int pageIndex;

    private int pageSize;

    private long totalPage;

    private Long total;

    private String sort;

    public static final int PAGE_MAX_DATA = 1000000;

    @Comment("Map → value 为复杂对象需做处理，如：{region:{id:1,name:'xxxx'}},则需转换成：region_name 前端才可正常渲染")
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
