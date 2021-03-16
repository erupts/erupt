package xyz.erupt.upms.constant;

import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2021/3/16 14:31
 */
public enum MenuLimitEnum implements TagsFetchHandler {
    ADD, DELETE, UPDATE, QUERY;

    @Override
    public List<String> fetchTags(String[] params) {
        List<String> list = new ArrayList<>();
        for (MenuLimitEnum value : MenuLimitEnum.values()) {
            list.add(value.name());
        }
        return list;
    }
}
