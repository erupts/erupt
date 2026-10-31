package xyz.erupt.share.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.interceptor.SessionScopeGuard;
import xyz.erupt.share.vo.ShareScope;
import xyz.erupt.upms.constant.EruptReqHeaderConst;

import java.util.List;
import java.util.Map;

/**
 * What a share session may call; everything else under /erupt-api answers 403.
 * <p>
 * A page session holds one menu and no button, so a route verified by menu or by erupt is already
 * what a user holding that menu alone could call, and passes. A login-level route is open to every
 * account, so it passes only when it declares the right it exercises ({@code @EruptRouter(power)}):
 * the semantic layer's reads do, a password change does not. A route behind {@code @EruptLoginAuth}
 * declares nothing and is refused.
 * <p>
 * A form is the one target that shows less than its menu grants: the menu lets a user read the
 * table, the link must not. Its session may call the build, the add endpoint and the field helpers
 * a form uses (options, references, uploads, onchange) of its erupt or of a sub-form it vouches
 * for, listed here, and nothing else.
 *
 * @author YuePeng
 */
@Component
public class ShareScopeGuard implements SessionScopeGuard {

    private static final String ERUPT = "erupt";

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    // Every route a form may reach, each naming the erupt it serves as {erupt}
    private static final List<String> FORM_ROUTES = List.of(
            EruptRestPath.ERUPT_BUILD + "/{erupt}",
            EruptRestPath.ERUPT_BUILD + "/{erupt}/{field}",
            EruptRestPath.ERUPT_DATA_MODIFY + "/{erupt}",
            EruptRestPath.ERUPT_DATA_MODIFY + "/tab-add/{erupt}/{tab}",
            EruptRestPath.ERUPT_DATA_MODIFY + "/tab-update/{erupt}/{tab}",
            EruptRestPath.ERUPT_DATA_MODIFY + "/tab-delete/{erupt}/{tab}",
            EruptRestPath.ERUPT_DATA + "/init-value/{erupt}",
            EruptRestPath.ERUPT_DATA + "/{erupt}/checkbox/{field}",
            EruptRestPath.ERUPT_DATA + "/{erupt}/reference-tree/{field}",
            EruptRestPath.ERUPT_DATA + "/{erupt}/reference-table/{field}",
            EruptRestPath.ERUPT_DATA + "/onchange/{erupt}/{field}",
            EruptRestPath.ERUPT_DATA + "/tab/tree/{erupt}/{field}",
            EruptRestPath.ERUPT_COMP + "/choice-item/{erupt}/{field}",
            EruptRestPath.ERUPT_COMP + "/choice-item-filter/{erupt}/{field}",
            EruptRestPath.ERUPT_COMP + "/tags-item/{erupt}/{field}",
            EruptRestPath.ERUPT_COMP + "/auto-complete/{erupt}/{field}",
            EruptRestPath.ERUPT_COMP + "/code-edit-hints/{erupt}/{field}",
            EruptRestPath.ERUPT_FILE + "/upload/{erupt}/{field}",
            EruptRestPath.ERUPT_FILE + "/uploads/{erupt}/{field}",
            EruptRestPath.ERUPT_FILE + "/upload-html-editor/{erupt}/{field}",
            EruptRestPath.ERUPT_FILE + "/upload-ueditor/{erupt}/{field}",
            EruptRestPath.ERUPT_API + "/tpl/html-field/{erupt}/{field}"
    );

    @Resource
    private EruptShareService eruptShareService;

    @Override
    public boolean allow(String token, HttpServletRequest request, EruptRouter eruptRouter) {
        ShareScope scope = eruptShareService.scope(token);
        if (null == scope) return true;
        if (scope.isForm()) return formAllows(scope.getTarget(), request, request.getServletPath());
        if (null == eruptRouter) return false;
        return eruptRouter.verifyType() != EruptRouter.VerifyType.LOGIN || eruptRouter.power().length > 0;
    }

    // The erupt in the path must be the shared one, or a sub-form of it: the interceptor has
    // already verified that the parent named in the header really nests the erupt in the path
    private static boolean formAllows(String erupt, HttpServletRequest request, String path) {
        for (String route : FORM_ROUTES) {
            if (!MATCHER.match(route, path)) continue;
            Map<String, String> vars = MATCHER.extractUriTemplateVariables(route, path);
            if (erupt.equalsIgnoreCase(vars.get(ERUPT))) return true;
            String parent = request.getHeader(EruptReqHeaderConst.ERUPT_PARENT_HEADER_KEY);
            if (null == parent) parent = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARENT_PARAM_KEY);
            return erupt.equalsIgnoreCase(parent);
        }
        return false;
    }
}
