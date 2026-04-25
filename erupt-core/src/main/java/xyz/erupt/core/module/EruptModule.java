package xyz.erupt.core.module;

import xyz.erupt.annotation.config.Comment;

import java.util.List;

/**
 * @author YuePeng
 * date 2022/1/4 20:57
 */
public interface EruptModule {

    @Comment("Module information")
    ModuleInfo info();

    @Comment("Initialize")
    default void run() {

    }

    @Comment("Initialize menus → executed only once; marker file location: .erupt/.${moduleName}")
    default List<MetaMenu> initMenus() {
        return null;
    }

    @Comment("Initialization method → executed only once; marker file location: .erupt/.${moduleName}")
    default void initFun() {

    }

}
