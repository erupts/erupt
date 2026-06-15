package xyz.erupt.monitor.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.redis.RedisInfo;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.util.Objects;

/**
 * @author YuePeng
 * date 2021/1/29 14:42
 */
@Slf4j
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/redis")
public class MonitorRedisController {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/info")
    @EruptMenuAuth(MonitorConstant.MENU_REDIS)
    public RedisInfo redisInfo() {
        if (eruptProp.isRedisSession()) {
            return new RedisInfo(Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()));
        } else {
            log.warn("redis session is not enabled");
            return new RedisInfo();
        }
    }

}
