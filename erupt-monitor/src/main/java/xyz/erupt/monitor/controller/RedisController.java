package xyz.erupt.monitor.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.monitor.constant.MonitorConstant;
import xyz.erupt.monitor.vo.redis.RedisInfo;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author liyuepeng
 * @date 2021/1/29 14:42
 */
@RestController
@RequestMapping(MonitorConstant.REST_MONITOR + "/redis.html")
public class RedisController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("/info")
    public RedisInfo redisInfo() {
        return new RedisInfo(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
    }

}
