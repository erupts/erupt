package xyz.erupt.core.constant;

import lombok.Getter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum MenuTypeEnum {

    TABLE("table", "表格视图", "填erupt类名"),
    TREE("tree", "树状视图", "填erupt类名"),
    BUTTON("button", "功能按钮", null),
    API("api", "接口名称", null),
    LINK("link", "框架内打开链接", "互联网地址"),
    NEW_WINDOW("newWindow", "新窗口打开链接", "互联网地址"),
    THIS_WINDOW("selfWindow", "本窗口打开链接", "互联网地址"),
    FILL("fill", "充满屏幕", "需要充满屏幕的路由地址"),
    ROUTER("router", "页面路由", "前端开发时的路由地址");

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

    public static class ChoiceFetch implements ChoiceFetchHandler {

        @Override
        public List<VLModel> fetch(String[] params) {
            return menuTypes.stream().map(it -> new VLModel(it.getValue(), it.getLabel(), it.getDesc())).collect(Collectors.toList());
        }

    }

}
