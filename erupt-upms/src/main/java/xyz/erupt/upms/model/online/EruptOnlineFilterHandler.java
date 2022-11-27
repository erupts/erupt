package xyz.erupt.upms.model.online;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.constant.SessionKey;

import javax.annotation.Resource;
import java.util.Set;

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

    @Override
    public String filter(String condition, String[] params) {
        if (eruptProp.isRedisSession()) {
            Set<String> keys = stringRedisTemplate.keys(SessionKey.TOKEN_OLINE + "*");
            if (keys != null && keys.size() > 0) {
                StringBuilder sb = new StringBuilder(EruptOnline.class.getSimpleName() + ".token in (");
                keys.forEach(it -> sb.append("'").append(it.substring(SessionKey.TOKEN_OLINE.length())).append("',"));
                return sb.substring(0, sb.length() - 1) + ")";
            }
            return "1 = 2";
        }
        throw new EruptApiErrorTip(EruptApiModel.Status.INFO,
                "Enable the RedisSession configuration to use this feature", EruptApiModel.PromptWay.NOTIFY);
    }
}
