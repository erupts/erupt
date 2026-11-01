package xyz.erupt.core.module;

/**
 * Where a module sits in the product: the module center lists modules by it, and the sidebar
 * titles the root menus of each group so a newcomer reads the menu as a map rather than a list.
 *
 * @author YuePeng
 * date 2026-11-01
 */
public enum ModuleGroup {
    // the foundation every installation has: users, permissions, security, templates
    PLATFORM,
    // what business users open every day: reports, workflow, notices, printing, files
    BUSINESS,
    // large-model features
    AI,
    // tools for the people who build the application
    DEV_TOOLS,
    // running and observing the installation
    OPS,
    // data source connectors
    DATA
}
