package xyz.erupt.job.service;

import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.toolkit.notify.DataAction;
import xyz.erupt.toolkit.notify.NotifyData;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class JobMessageListener {

    public static final String JOB_TOPIC = "erupt-job:data-proxy:notify";

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Resource
    private EruptProp eruptProp;

    @PostConstruct
    public void post() {
        if (eruptProp.isRedisSession()) {
            redisConnectionFactory.getConnection().subscribe((message, pattern) -> {
                NotifyData<EruptJob> notifyData = GsonFactory.getGson().fromJson(message.toString(), new TypeToken<NotifyData<EruptJob>>() {
                }.getType());
                if (DataAction.ADD.equals(notifyData.getAction())) {

                } else if (DataAction.UPDATE.equals(notifyData.getAction())) {

                } else if (DataAction.DELETE.equals(notifyData.getAction())) {

                }
            }, JOB_TOPIC.getBytes());
        }
    }

}
