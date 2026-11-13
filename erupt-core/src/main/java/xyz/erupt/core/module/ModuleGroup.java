package xyz.erupt.core.module;

import lombok.Getter;
import xyz.erupt.core.i18n.I18nTranslate;

import java.util.Objects;

/**
 * Where a module sits in the product: the module center lists modules by it, and the sidebar
 * titles the root menus of each group so a newcomer reads the menu as a map rather than a list.
 *
 * <p>A value object: the constants below cover the product's own modules, an application declares
 * its own group with {@link #of(String)} — say {@code of("CRM")} — and its root menus then form a
 * section of that name. The title is translated server side, like every other menu text: the
 * English title doubles as the i18n key, so a custom group without a translation row simply shows
 * its title, and one ships translations by adding a row to any module's CSV.
 *
 * @author YuePeng
 * date 2026-11-01
 */
@Getter
public final class ModuleGroup implements Comparable<ModuleGroup> {

    // the foundation every installation has: users, permissions, security, templates
    public static final ModuleGroup PLATFORM = new ModuleGroup("Platform", 0);
    // what business users open every day: reports, workflow, notices, printing, files
    public static final ModuleGroup BUSINESS = new ModuleGroup("Business", 10);
    // large-model features
    public static final ModuleGroup AI = new ModuleGroup("AI", 20);
    // tools for the people who build the application
    public static final ModuleGroup DEV_TOOLS = new ModuleGroup("Developer tools", 30);
    // running and observing the installation
    public static final ModuleGroup OPS = new ModuleGroup("Operations", 40);
    // data source connectors
    public static final ModuleGroup DATA = new ModuleGroup("Data sources", 50);
    // sample models that show what the framework can do, such as erupt-demo
    public static final ModuleGroup DEMO = new ModuleGroup("Demo", 60);

    // custom groups take this order, so they list after every built-in one
    private static final int CUSTOM_ORDER = 1000;

    // the English title doubles as the i18n key, per the CSV convention
    private final String title;

    // the module center's section order
    private final int order;

    private ModuleGroup(String title, int order) {
        this.title = Objects.requireNonNull(title);
        this.order = order;
    }

    /** A group of the application's own; the same title is the same group. */
    public static ModuleGroup of(String title) {
        return new ModuleGroup(title, CUSTOM_ORDER);
    }

    /** The group's title translated for the current request. */
    public String title() {
        return I18nTranslate.$translate(title);
    }

    // built-ins in their declared order, customs alphabetically behind them
    @Override
    public int compareTo(ModuleGroup other) {
        int byOrder = Integer.compare(this.order, other.order);
        return 0 != byOrder ? byOrder : this.title.compareTo(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleGroup that)) return false;
        return this.title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return this.title.hashCode();
    }

    @Override
    public String toString() {
        return this.title;
    }

}
