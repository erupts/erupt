package xyz.erupt.core.usage;

import java.util.List;

/**
 * Self-registering answer to "what is this model used for".
 * <p>
 * A module that lets a user bind a model to something — a flow to its form, a flow node to its
 * config model, a menu to its table, a dashboard to its cube — declares those bindings here, and
 * erupt-atlas reads them backwards: open a model, and see everything that would have to be
 * reopened if it changed. The module owns the knowledge; the atlas owns none of it.
 * <p>
 * Register by declaring the implementation a Spring bean. A provider that has nothing to say, or
 * whose tables are not there yet, returns an empty list rather than failing: the atlas asks every
 * provider on every request and one silent module must not cost the page.
 *
 * @author YuePeng
 */
public interface EruptUsageProvider {

    List<EruptUsage> usages();

}
