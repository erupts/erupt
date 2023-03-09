package xyz.erupt.flow.web;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageInfo;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 继承并扩展能力
 */
public class EruptApiPageModel extends EruptApiModel {

    public EruptApiPageModel(Status status, String message, Object data, PromptWay promptWay) {
        super(status, message, data, promptWay);
    }

    /**
     * 为分页查询返回结果
     * @param list
     * @param <T>
     * @return
     */
    public static <T> EruptApiModel successApi(List<T> list) {
        PageInfo page = new PageInfo(list);//转化为pageInfo
        List<Map<String, Object>> mapList = list.stream().map(t -> {
            Map<String, Object> map = BeanUtil.beanToMap(t);
            return map;
        }).collect(Collectors.toList());

        Page p = new Page();
        p.setList(mapList);
        p.setPageIndex(page.getPageNum());
        p.setPageSize(page.getPageSize());
        p.setTotal(page.getTotal());
        p.setTotalPage(page.getPages());//总页数

        return new EruptApiModel(Status.SUCCESS, null, p, PromptWay.MESSAGE);
    }
}
