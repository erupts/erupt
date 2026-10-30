package xyz.erupt.share.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.security.scope.SessionScopeGuard;
import xyz.erupt.share.model.EruptShareLink;
import xyz.erupt.share.vo.ShareScope;
import xyz.erupt.upms.constant.EruptReqHeaderConst;

import java.util.List;
import java.util.Map;

/**
 * What a share session may call. A public form needs the build of its erupt, the add endpoint,
 * the field helpers a form uses (options, references, uploads, onchange) and nothing else: no
 * table query, no record read, no update, no export, no other erupt. A public page needs its own
 * template and nothing else. Everything else under /erupt-api answers 403.
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

    private static final String TPL = EruptRestPath.ERUPT_API + "/tpl/";

    @Resource
    private EruptShareService eruptShareService;

    @Override
    public boolean allow(String token, HttpServletRequest request, EruptRouter eruptRouter) {
        ShareScope scope = eruptShareService.scope(token);
        if (null == scope) return true;
        String path = request.getServletPath();
        if (EruptShareLink.TYPE_FORM.equals(scope.getType())) return formAllows(scope, request, path);
        if (EruptShareLink.TYPE_PAGE.equals(scope.getType())) return path.equals(TPL + scope.getPath());
        return false;
    }

    // The erupt in the path must be the shared one, or a sub-form of it: the interceptor has
    // already verified that the parent named in the header really nests the erupt in the path
    private boolean formAllows(ShareScope scope, HttpServletRequest request, String path) {
        for (String route : FORM_ROUTES) {
            if (!MATCHER.match(route, path)) continue;
            Map<String, String> vars = MATCHER.extractUriTemplateVariables(route, path);
            String erupt = vars.get(ERUPT);
            if (scope.getErupt().equalsIgnoreCase(erupt)) return true;
            String parent = request.getHeader(EruptReqHeaderConst.ERUPT_PARENT_HEADER_KEY);
            if (null == parent) parent = request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARENT_PARAM_KEY);
            return scope.getErupt().equalsIgnoreCase(parent);
        }
        return false;
    }
}
