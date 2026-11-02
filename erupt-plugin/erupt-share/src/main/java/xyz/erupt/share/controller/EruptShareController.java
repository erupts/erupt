package xyz.erupt.share.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.R;
import xyz.erupt.share.service.EruptShareService;
import xyz.erupt.share.vo.ShareExchangeInput;
import xyz.erupt.share.vo.ShareMetaVo;
import xyz.erupt.share.vo.ShareSessionVo;

/**
 * The two calls a visitor makes before holding a session, so neither carries {@code @EruptRouter}:
 * what the link is, and the session for it. Everything after that goes through the ordinary
 * erupt APIs under the minted token.
 *
 * @author YuePeng
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/share")
public class EruptShareController {

    @Resource
    private EruptShareService eruptShareService;

    /**
     * Describe a share link.
     *
     * @apiNote Title, type, issuer and whether a password is asked; available = false with a translated reason when the link cannot be opened. Public.
     * @param code The code in the link
     * @return link description
     */
    @GetMapping("/meta/{code}")
    public R<ShareMetaVo> meta(@PathVariable("code") String code) {
        return R.ok(eruptShareService.meta(code));
    }

    /**
     * Open a share link.
     *
     * @apiNote Trades the code (and password) for a session token scoped to the link's target, plus the frontend route that renders it. Attempts are rate limited per client address. Public.
     * @param code The code in the link
     * @param input Password, when the link asks one
     * @return token, route and remaining minutes; an error status with the reason otherwise
     */
    @PostMapping("/exchange/{code}")
    public R<ShareSessionVo> exchange(@PathVariable("code") String code, @RequestBody(required = false) ShareExchangeInput input) {
        try {
            return R.ok(eruptShareService.exchange(code, null == input ? null : input.getPassword()));
        } catch (EruptWebApiRuntimeException e) {
            // the landing page shows the reason in place; no global error dialog
            return R.errorNotIntercept(e.getMessage(), null);
        }
    }
}
