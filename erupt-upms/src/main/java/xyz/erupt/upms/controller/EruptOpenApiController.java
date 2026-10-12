package xyz.erupt.upms.controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOpenApi;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.vo.OpenApiInfoVo;
import xyz.erupt.upms.vo.OpenApiTokenVo;

import java.time.LocalDateTime;

/**
 * Open API token
 *
 * @apiNote Token exchange for server-to-server callers registered as Open API apps.
 *
 * @author YuePeng
 * date 2024/8/4 22:05
 */
@RestController
@AllArgsConstructor
@RequestMapping(EruptRestPath.ERUPT_API + "/open-api")
@Slf4j
public class EruptOpenApiController {

    private final EruptDao eruptDao;

    private final EruptTokenService eruptTokenService;

    /**
     * Look up the app behind a token.
     *
     * @param token Token issued by create-token
     * @return appid and app name
     */
    @GetMapping("/get-appid")
    public R<OpenApiInfoVo> getAppid(@RequestParam("token") String token) {
        if (!eruptTokenService.tokenExist(token)) throw new EruptWebApiRuntimeException("token expired or not found");
        EruptOpenApi eruptOpenApi = eruptDao.lambdaQuery(EruptOpenApi.class).eq(EruptOpenApi::getCurrentToken, token).one();
        if (eruptOpenApi == null) throw new EruptWebApiRuntimeException("token not found");
        return R.ok(new OpenApiInfoVo(eruptOpenApi.getAppid(), eruptOpenApi.getName()));
    }

    /**
     * Issue a token for an Open API app.
     *
     * @apiNote The token carries the permissions of the user bound to the app and is sent like a login token. Only one token per appid is valid at a time: issuing a new one revokes the previous.
     * @param appid Application id
     * @param secret Application secret
     * @return token and its expiry
     */
    @GetMapping("/create-token")
    @Transactional
    public R<OpenApiTokenVo> createToken(@RequestParam("appid") String appid, @RequestParam("secret") String secret) {
        EruptOpenApi eruptOpenApi = eruptDao.lambdaQuery(EruptOpenApi.class).eq(EruptOpenApi::getAppid, appid).one();
        if (eruptOpenApi == null) throw new EruptWebApiRuntimeException("appid not found");
        if (!secret.equals(eruptOpenApi.getSecret())) throw new EruptWebApiRuntimeException("secret error");
        if (!eruptOpenApi.getStatus()) throw new EruptWebApiRuntimeException("locked down");
        String token = "ER" + Erupts.generateCode(24).toUpperCase();
        LocalDateTime expire = LocalDateTime.now().plusMinutes(eruptOpenApi.getExpire());
        eruptTokenService.loginToken(eruptOpenApi.getEruptUser(), token, eruptOpenApi.getExpire());
        if (null != eruptOpenApi.getCurrentToken()) {
            log.info("open-api remove old token {}", eruptOpenApi.getName());
            eruptTokenService.logoutToken(eruptOpenApi.getName(), eruptOpenApi.getCurrentToken());
        }
        eruptOpenApi.setCurrentToken(token);
        return R.ok(new OpenApiTokenVo(token, expire));
    }

}
