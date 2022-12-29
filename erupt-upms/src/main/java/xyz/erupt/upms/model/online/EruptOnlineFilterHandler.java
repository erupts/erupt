package xyz.erupt.upms.model.online;

import org.apache.commons.lang3.StringUtils;
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
            /*
            Set<String> keys = stringRedisTemplate.keys(SessionKey.TOKEN_OLINE + "*");
            if (keys != null && keys.size() > 0) {
                StringBuilder sb = new StringBuilder(EruptOnline.class.getSimpleName() + ".token in (");
                keys.forEach(it -> sb.append("'").append(it.substring(SessionKey.TOKEN_OLINE.length())).append("',"));
                return sb.substring(0, sb.length() - 1) + ")";
            }
            */
            // xpc begin
            Set<String> keys = stringRedisTemplate.keys(SessionKey.TOKEN_OLINE + "*");
            if (keys != null && keys.size() > 0) {
                String className = EruptOnline.class.getSimpleName();
                StringBuilder sb = new StringBuilder();
                sb.append("(");

                //解决ORA-01795问题
                int inNum = 1;
                sb.append(className + ".token in (");
                for (String key : keys) {
                    if (StringUtils.isNotBlank(key)) {
                        String keyVal = key.substring(SessionKey.TOKEN_OLINE.length());

                        if ((inNum % 500) == 0) {
                            sb.append("'" + keyVal + "' ) OR " + className + ".token IN ( ");
                        } else {
                            if (inNum == keys.size()) {
                                sb.append("'" + keyVal + "') ");
                            } else {
                                sb.append("'" + keyVal + "', ");
                            }
                        }
                    }
                    inNum++;
                }
                sb.append(")");
                return sb.toString();
            }
            // xpc end
            return "1 = 2";
        }
        throw new EruptApiErrorTip(EruptApiModel.Status.INFO,
                "Enable the RedisSession configuration to use this feature", EruptApiModel.PromptWay.NOTIFY);
    }
}
