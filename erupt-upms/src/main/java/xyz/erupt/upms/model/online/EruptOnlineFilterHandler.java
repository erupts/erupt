package xyz.erupt.upms.model.online;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.linq.lambda.LambdaSee;
import xyz.erupt.upms.constant.SessionKey;
import xyz.erupt.upms.service.EruptSessionService;

import jakarta.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2021/2/16 14:42
 */
@Service
public class EruptOnlineFilterHandler implements FilterHandler {

    @Resource
    private EruptSessionService eruptSessionService;

    @Override
    public String filter(String condition, String[] params) {
        Set<String> keys = eruptSessionService.keys(SessionKey.TOKEN_OLINE);
        if (!keys.isEmpty()) {
            return EruptOnline.class.getSimpleName() + "." + LambdaSee.field(EruptOnline::getToken) + " in (" + keys.stream()
                    .map(it -> "'" + it.substring(SessionKey.TOKEN_OLINE.length()) + "'").collect(Collectors.joining(",")) + ")";
        } else {
            return "1 = 2";
        }
    }

}
