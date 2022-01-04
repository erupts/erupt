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
    default void init() {

    }

    @Comment("功能菜单")
    default List<MetaMenu> menus() {
        return null;
    }

}
