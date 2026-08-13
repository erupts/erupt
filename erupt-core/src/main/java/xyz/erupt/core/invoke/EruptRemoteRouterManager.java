package xyz.erupt.core.invoke;

import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.service.EruptRemoteRouter;

/**
 * Static holder for the optional {@link EruptRemoteRouter} (mirrors {@link DataProcessorManager}).
 * erupt-cloud-server registers its implementation at startup; core stays free of any cloud dependency.
 *
 * @author YuePeng
 */
public class EruptRemoteRouterManager {

    private static EruptRemoteRouter router;

    public static void register(EruptRemoteRouter eruptRemoteRouter) {
        router = eruptRemoteRouter;
    }

    public static EruptRemoteRouter get() {
        return router;
    }

    /**
     * Cheap gate for the hot path: local erupt names are simple class names (no dot),
     * so only dotted names that miss the local registry ever reach the router.
     */
    public static boolean isRemote(String eruptName) {
        return null != router && null != eruptName
                && eruptName.indexOf(EruptConst.DOT.charAt(0)) > 0
                && router.isRemote(eruptName);
    }

}
