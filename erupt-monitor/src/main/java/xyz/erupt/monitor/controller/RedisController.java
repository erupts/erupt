package xyz.erupt.monitor.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.redis.RedisInfo;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author YuePeng
 * date 2021/1/29 14:42
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/redis.html")
public class RedisController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/info")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public RedisInfo redisInfo() {
        return new RedisInfo(Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()));
    }

}
