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

    public static final int PAGE_MAX_DATA = 1000000;

    private Integer pageIndex;

    private Integer pageSize;

    private String sort;

    //总页数
    private Integer totalPage;

    //总条数
    private Long total;

    @Comment("Map → value 为复杂对象需做特殊处理，如：{region:{id:1,name:'xxxx'}},则需转换成：region_name 前端才可正常渲染")
    private Collection<Map<String, Object>> list;

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

    public Page() {
    }
}
