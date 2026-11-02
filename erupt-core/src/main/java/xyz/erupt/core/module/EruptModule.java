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

    @Comment("Menus of the module → reconciled into e_upms_menu on start whenever this definition changes; admin edits are kept")
    default List<MetaMenu> initMenus() {
        return null;
    }

    @Comment("Initialization method → executed once per database, before the module's first menu seeding (tracked in e_upms_module)")
    default void initFun() {

    }

}
