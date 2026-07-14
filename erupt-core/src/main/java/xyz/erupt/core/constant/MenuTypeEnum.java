package xyz.erupt.core.constant;

import lombok.Getter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum MenuTypeEnum {

    TABLE("table", "Table View", "Erupt Class Name"),
    TREE("tree", "Tree View", "Erupt Class Name"),
    FORM("form", "Form View", "Erupt Class Name"),
    BUTTON("button", "Function Button", null),
    API("api", "Interface Name", null),
    LINK("link", "Open Link in Frame", "URL"),
    NEW_WINDOW("newWindow", "Open Link in New Window", "URL"),
    THIS_WINDOW("selfWindow", "Open Link in Current Window", "URL"),
    FILL("fill", "Full Screen", "Fill Router Address"),
    ROUTER("router", "Page Route", "Router Address");

    private final String code;
    private final String name;
    private final String desc;

    private static final List<VLModel> menuTypes;

    static {
        menuTypes = Stream.of(MenuTypeEnum.values()).map(menuTypeEnum ->
                new VLModel(menuTypeEnum.getCode(), menuTypeEnum.getName(), menuTypeEnum.getDesc())).collect(Collectors.toList());
    }

    MenuTypeEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static void addMenuType(VLModel menuType) {
        menuTypes.add(menuType);
    }

    public static class ChoiceFetch implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return menuTypes.stream().map(it -> new VLModel(it.getValue(), it.getLabel(), it.getDesc())).collect(Collectors.toList());
        }

    }

}
