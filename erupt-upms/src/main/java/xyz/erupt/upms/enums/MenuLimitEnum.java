package xyz.erupt.upms.enums;

import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2021/3/16 14:31
 */
public enum MenuLimitEnum {
    NO_ADD, NO_DELETE, NO_EDIT, NO_QUERY, NO_EXPORT, NO_IMPORT;

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
