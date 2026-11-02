package xyz.erupt.core.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.model.Alert;
import xyz.erupt.annotation.query.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 2018-10-12.
 */
@Getter
@Setter
@NoArgsConstructor
public class Page {

    public static final int PAGE_MAX_DATA = 1000000;

    /** 1-based page number */
    private Integer pageIndex;

    /** Rows per page */
    private Integer pageSize;

    /** Sort fields in priority order */
    private List<Sort> sort;

    /** Number of pages, response only */
    private Integer totalPage;

    /** Number of rows matched, response only */
    private Long total;

    @Comment("Map → when the value is a complex object, special handling is required. For example: {region:{id:1,name:'xxxx'}} must be converted to region_name for the frontend to render correctly")
    /** Rows keyed by field name, response only */
    private Collection<Map<String, Object>> list;

    /** Banner to show above the table, response only */
    private Alert alert;

    /** Extra HTML rendered under the table, response only */
    private String extraContent;

    public void setTotal(Long total) {
        this.total = total;
        if (total % pageSize == 0) {
            totalPage = total.intValue() / pageSize;
        } else {
            totalPage = total.intValue() / pageSize + 1;
        }
    }

    public Page(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}
