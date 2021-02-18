package xyz.erupt.monitor.model;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.upms.constant.SessionKey;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2021/2/16 14:42
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
            Set<String> keys = stringRedisTemplate.keys(SessionKey.USER_TOKEN + "*");
            if (keys != null && keys.size() > 0) {
                StringBuilder sb = new StringBuilder(EruptOnline.class.getSimpleName() + ".token in (");
                for (String key : keys) {
                    sb.append("'").append(key.substring(SessionKey.USER_TOKEN.length())).append("',");
                }
                return sb.substring(0, sb.length() - 1) + ")";
            } else {
                return "1 = 2";
            }
        } else {
            throw new EruptApiErrorTip(new EruptApiModel(
                    EruptApiModel.Status.INFO,
                    "启用 redisSession 配置可使用该功能",
                    EruptApiModel.PromptWay.NOTIFY));
        }
    }
}
