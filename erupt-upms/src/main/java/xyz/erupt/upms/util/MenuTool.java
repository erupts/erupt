package xyz.erupt.upms.util;

import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.upms.constant.MenuTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class MenuTool {

    private static final List<VLModel> menuTypes = new ArrayList<>();

    static {
        for (MenuTypeEnum menuTypeEnum : MenuTypeEnum.values()) {
            menuTypes.add(new VLModel(menuTypeEnum.getCode(), menuTypeEnum.getName(), menuTypeEnum.getDesc()));
        }
    }

    /**
     * 添加menu类型
     */
    public static void addMenuType(VLModel menuType) {
        menuTypes.add(menuType);
    }

    public static List<VLModel> getMenuTypes() {
        return menuTypes;
    }
}
