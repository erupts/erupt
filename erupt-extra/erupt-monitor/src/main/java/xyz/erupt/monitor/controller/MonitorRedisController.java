package xyz.erupt.monitor.controller;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.redis.RedisInfo;

import java.util.Objects;

/**
 * @author YuePeng
 * date 2021/1/29 14:42
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/redis.html")
public class MonitorRedisController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/info")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU)
    public RedisInfo redisInfo() {
        return new RedisInfo(Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()));
    }

}
