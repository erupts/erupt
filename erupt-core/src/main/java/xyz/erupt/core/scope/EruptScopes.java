package xyz.erupt.core.scope;

import xyz.erupt.core.util.EruptSpringUtil;

/**
 * Reads the scope the current request runs in, already verified:
 * <pre>
 * KubeScope scope = EruptScopes.get(KubeScope.class);
 * </pre>
 * Falls back to the scope's defaults when the request carries no pick.
 *
 * @author YuePeng
 */
public final class EruptScopes {

    private EruptScopes() {
    }

    public static <T> T get(Class<T> scope) {
        return EruptSpringUtil.getBean(EruptScopeService.class).get(scope);
    }

}
