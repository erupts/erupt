package xyz.erupt.annotation;

import java.lang.annotation.*;

/**
 * Places a model inside a scope: a global context (a project, a cluster, an environment) the user
 * picks once in the page header and every scoped table then follows.
 * <p>
 * The scope itself is a plain {@link Erupt} form class; each of its fields is one condition, so
 * options, cascades, defaults and validation come from the ordinary form annotations.
 * <p>
 * The values come from the client. CHOICE and MULTI_CHOICE values are checked against the options
 * the current user is offered; any other field is taken as typed. A scope only narrows what the
 * model's menu already shows, so to keep users apart (a project they may not see) use a choice
 * whose options are filtered per user, or {@code @Filter} / data permissions.
 * <pre>
 * &#64;Erupt(name = "Workspace")
 * public class KubeScope {
 *     &#64;EruptField(edit = &#64;Edit(title = "Cluster", type = EditType.CHOICE, notNull = true,
 *             choiceType = &#64;ChoiceType(fetchHandler = KubeContextFetchHandler.class)))
 *     private String cluster;
 * }
 *
 * &#64;EruptScope(KubeScope.class)
 * public class KubePod { ... }
 * </pre>
 *
 * @author YuePeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EruptScope {

    /**
     * The scope form class.
     */
    Class<?> value();

    /**
     * Bind the model's fields that share a name with the scope's fields: lists and details are
     * filtered by the picked value, adds and edits are forced to it. Turn off when the data source
     * reads the scope itself through {@code EruptScopes.get(...)}.
     */
    boolean bind() default true;

}
