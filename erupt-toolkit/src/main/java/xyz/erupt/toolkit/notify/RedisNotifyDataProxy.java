package xyz.erupt.toolkit.notify;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyContext;
import xyz.erupt.core.prop.EruptProp;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2024/6/29 18:22
 */
@Component
public class RedisNotifyDataProxy implements DataProxy<Object> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EruptProp eruptProp;

    @Override
    public void afterAdd(Object o) {
        this.publish(DataAction.ADD, o);
    }


    @Override
    public void afterUpdate(Object o) {
        this.publish(DataAction.UPDATE, o);
    }

    @Override
    public void afterDelete(Object o) {
        this.publish(DataAction.DELETE, o);
    }

    private void publish(DataAction action, Object data) {
        if (eruptProp.isRedisSession()) {
            if (DataProxyContext.params().length == 0 || null == DataProxyContext.params()[0]) throw new EruptWebApiRuntimeException("DataProxy params[0] not found → redis channel");
            stringRedisTemplate.convertAndSend(DataProxyContext.params()[0], GsonFactory.getGson().toJson(new NotifyData<>(action, data)));
        }
    }

}
