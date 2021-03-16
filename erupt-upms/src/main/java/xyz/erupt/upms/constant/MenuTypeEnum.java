package xyz.erupt.upms.constant;

import lombok.Getter;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum MenuTypeEnum {

    TABLE("table", "表格", "填erupt类名"),
    TREE("tree", "树", "填erupt类名"),
    LINK("link", "链接", "互联网地址"),
    NEW_WINDOW("newWindow", "新页签", "互联网地址"),
    FILL("fill", "充满屏幕", "路由地址"),
    ROUTER("router", "页面路由", null),
    BUTTON("button", "功能按钮", null),
    API("api", "接口名称", null);

    private final String code;
    private final String name;
    private final String desc;

    MenuTypeEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    private static final List<VLModel> menuTypes = new ArrayList<>();

    static {
        for (MenuTypeEnum menuTypeEnum : MenuTypeEnum.values()) {
            menuTypes.add(new VLModel(menuTypeEnum.getCode(), menuTypeEnum.getName(), menuTypeEnum.getDesc()));
        }
    }

    public static void addMenuType(VLModel menuType) {
        menuTypes.add(menuType);
    }

    public static class ChoiceFetch implements ChoiceFetchHandler {

        @Override
        public List<VLModel> fetch(String[] params) {
            return menuTypes;
        }

    }

}
