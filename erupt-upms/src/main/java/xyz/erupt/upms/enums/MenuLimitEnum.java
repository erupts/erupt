package xyz.erupt.upms.enums;

import xyz.erupt.annotation.fun.TagsFetchHandler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2021/3/16 14:31
 */
public enum MenuLimitEnum {
    NO_ADD, NO_DELETE, NO_EDIT, NO_QUERY, NO_EXPORT, NO_IMPORT;

    public static class MenuLimitFetch implements TagsFetchHandler {

        @Override
        public List<String> fetchTags(String[] params) {
            return Stream.of(MenuLimitEnum.values())
                    .map(Enum::name).collect(Collectors.toList());
        }

    }

}
