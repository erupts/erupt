package xyz.erupt.core.module;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2022/1/4 20:57
 */
public interface EruptModule {

    @Comment("模块信息")
    ModuleInfo info();

    @Comment("初始化")
    default void run() {

    }

    @Comment("初始化菜单 → 仅执行一次，标识文件位置.erupt/.${moduleName}")
    default List<MetaMenu> initMenus() {
        return null;
    }

    @Comment("初始化方法 → 仅执行一次，标识文件位置.erupt/.${moduleName}")
    default void initFun() {

    }

}
