package xyz.erupt.upms.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptOpenApi;
import xyz.erupt.upms.service.EruptTokenService;
import xyz.erupt.upms.vo.OpenApiTokenVo;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

/**
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
     * 获取token
     * 每个 appid 同一时间只有一个 token 有效
     *
     * @param appid  appid
     * @param secret secret
     * @return token
     */
    @GetMapping("/create-token")
    @Transactional
    public R<OpenApiTokenVo> createToken(@RequestParam("appid") String appid, @RequestParam("secret") String secret) {
        EruptOpenApi eruptOpenApi = eruptDao.lambdaQuery(EruptOpenApi.class).eq(EruptOpenApi::getAppid, appid).one();
        if (eruptOpenApi == null) throw new EruptWebApiRuntimeException("appid not found");
        if (!secret.equals(eruptOpenApi.getSecret())) throw new EruptWebApiRuntimeException("secret error");
        if (!eruptOpenApi.getStatus()) throw new EruptWebApiRuntimeException("locked down");
        String token = "ER" + RandomStringUtils.randomAlphanumeric(24).toUpperCase();
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
