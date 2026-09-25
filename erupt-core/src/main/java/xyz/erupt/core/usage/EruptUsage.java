package xyz.erupt.core.usage;

import xyz.erupt.core.constant.MenuTypeEnum;

/**
 * One place a model is used, outside the model's own declaration.
 * <p>
 * A relation between two models is visible in the model itself — the field carries it. This is the
 * other half: a binding a user made at runtime, which lives in a configuration row and which no
 * annotation on the model will ever mention. A flow bound to its form, a flow node bound to its
 * config model, a menu opening a table, a dashboard reading a cube.
 * <p>
 * Name the target by its class wherever the code has one; the string form is for a name that was
 * stored rather than written, and a stored name that no longer resolves is itself worth seeing.
 *
 * @param target the erupt name, or the cube name, that is being used
 * @param type   which of the two it is: one class may carry both, and they are used for different things
 * @param usage  what the target is used as, already translated: "Flow node", "Menu display"
 * @param owner  the configuration that uses it, named the way its own page names it
 * @param route  console route that opens that configuration, or null when it has no page of its own
 * @author YuePeng
 */
public record EruptUsage(String target, Target type, String usage, String owner, String route) {

    /** A class annotated with both @Erupt and @EruptCube answers to one name and is two things */
    public enum Target {
        ERUPT, CUBE
    }

    public EruptUsage(String target, String usage, String owner) {
        this(target, Target.ERUPT, usage, owner, null);
    }

    public EruptUsage(String target, String usage, String owner, String route) {
        this(target, Target.ERUPT, usage, owner, route);
    }

    public EruptUsage(Class<?> target, String usage, String owner) {
        this(target.getSimpleName(), Target.ERUPT, usage, owner, null);
    }

    public EruptUsage(Class<?> target, String usage, String owner, String route) {
        this(target.getSimpleName(), Target.ERUPT, usage, owner, route);
    }

    /** Console route of a table view, which is where a configuration is maintained more often than not */
    public static String tableRoute(Class<?> erupt) {
        return "/build/" + MenuTypeEnum.TABLE.getCode() + "/" + erupt.getSimpleName();
    }

}
