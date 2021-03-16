package xyz.erupt.upms.constant;

import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2021/3/16 14:31
 */
public enum MenuLimitEnum {
    ADD, DELETE, UPDATE, QUERY;

    public static class MenuLimitFetch implements TagsFetchHandler {

        @Override
        public List<String> fetchTags(String[] params) {
            List<String> list = new ArrayList<>();
            for (MenuLimitEnum value : MenuLimitEnum.values()) {
                list.add(value.name());
            }
            return list;
        }
    }

}
