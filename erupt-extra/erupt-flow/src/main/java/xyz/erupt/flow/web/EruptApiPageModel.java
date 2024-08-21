package xyz.erupt.flow.web;

import com.github.pagehelper.PageInfo;
import org.springframework.cglib.beans.BeanMap;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.Page;

import java.util.HashMap;
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
     * 必须是mybatis分页
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> EruptApiModel successApi(List<T> list, int pageNum, int pageSize) {
        PageInfo page = new PageInfo(list);//转化为pageInfo
        List<Map<String, Object>> mapList = list.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            BeanMap beanMap = BeanMap.create(t);
            for (Object object : beanMap.entrySet()) {
                if (object instanceof Map.Entry) {
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
                    String key = entry.getKey();
                    map.put(key, beanMap.get(key));
                }
            }
            return map;
        }).collect(Collectors.toList());

        Page p = new Page();
        p.setList(mapList);
        p.setPageIndex(pageNum);
        p.setPageSize(pageSize);
        p.setTotal(page.getTotal());
        p.setTotalPage(page.getPages());//总页数

        return new EruptApiModel(Status.SUCCESS, null, p, PromptWay.MESSAGE);
    }
}
