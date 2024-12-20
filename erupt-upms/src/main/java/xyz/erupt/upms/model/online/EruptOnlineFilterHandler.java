package xyz.erupt.upms.model.online;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptLocalSession;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/2/16 14:42
 */
@Service
public class EruptOnlineFilterHandler implements FilterHandler {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EruptLocalSession eruptLocalSession;

    @Override
    public String filter(String condition, String[] params) {
        Set<String> keys;
        if (eruptProp.isRedisSession()) {
            keys = stringRedisTemplate.keys(SessionKey.TOKEN_OLINE + "*");
        } else {
            keys = eruptLocalSession.keySet().stream().filter(it -> it.startsWith(SessionKey.TOKEN_OLINE)).collect(Collectors.toSet());
        }
        if (keys != null && !keys.isEmpty()) {
            return EruptOnline.class.getSimpleName() + "." + LambdaSee.field(EruptOnline::getToken) + " in (" + keys.stream()
                    .map(it -> "'" + it.substring(SessionKey.TOKEN_OLINE.length()) + "'").collect(Collectors.joining(",")) + ")";
        } else {
            return "1 = 2";
        }
    }

}
