package xyz.erupt.core.module;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.constant.MenuTypeEnum;

/**
 * @author YuePeng
 * date 2022/1/4 20:57
 */
@Getter
@Setter
public class MetaMenu {

    private Long id; //无需传递此参数

    private String code;

    private String name;

    private MenuStatus status;

    private String type;

    private String value;

    private Integer sort;

    private String icon;

    private MetaMenu parentMenu;

    public MetaMenu() {
    }

    public static MetaMenu createRootMenu(String code, String name, String icon, Integer sort) {
        MetaMenu metaMenu = new MetaMenu();
        metaMenu.code = code;
        metaMenu.name = name;
        metaMenu.status = MenuStatus.OPEN;
        metaMenu.sort = sort;
        metaMenu.icon = icon;
        return metaMenu;
    }

    public static MetaMenu createSimpleMenu(String code, String name, String value, MetaMenu parent, Integer sort, String icon, String type, MenuStatus menuStatus) {
        MetaMenu metaMenu = new MetaMenu();
        metaMenu.code = code;
        metaMenu.name = name;
        metaMenu.status = menuStatus;
        metaMenu.type = type;
        metaMenu.value = value;
        metaMenu.sort = sort;
        metaMenu.parentMenu = parent;
        metaMenu.icon = icon;
        return metaMenu;
    }

    public static MetaMenu createSimpleMenu(String code, String name, String value, MetaMenu parent, Integer sort, String type) {
        return createSimpleMenu(code, name, value, parent, sort, null, type, MenuStatus.OPEN);
    }

    public static MetaMenu createEruptClassMenu(Class<?> eruptClass, MetaMenu parent, Integer sort, MenuTypeEnum menuTypeEnum, MenuStatus menuStatus) {
        return createSimpleMenu(eruptClass.getSimpleName(), eruptClass.getAnnotation(Erupt.class).name(),
                eruptClass.getSimpleName(), parent, sort, "", menuTypeEnum.getCode(), menuStatus);
    }

    public static MetaMenu createEruptClassMenu(Class<?> eruptClass, MetaMenu parent, Integer sort) {
        return createEruptClassMenu(eruptClass, parent, sort, MenuTypeEnum.TABLE, MenuStatus.OPEN);
    }

    public static MetaMenu createEruptClassMenu(Class<?> eruptClass, MetaMenu parent, Integer sort, MenuStatus menuStatus) {
        return createEruptClassMenu(eruptClass, parent, sort, MenuTypeEnum.TABLE, menuStatus);
    }

    public static MetaMenu createEruptClassMenu(Class<?> eruptClass, MetaMenu parent, Integer sort, MenuTypeEnum menuTypeEnum) {
        return createEruptClassMenu(eruptClass, parent, sort, menuTypeEnum, MenuStatus.OPEN);
    }

}
