package xyz.erupt.toolkit.notify;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyContext;

/**
 * @author YuePeng
 * date 2024/6/29 18:22
 */
@Component
public class RedisNotifyDataProxy implements DataProxy<Object> {

    @Resource
    private RedisNotifier redisNotifier;

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

    // after commit: a rolled back change is not announced, and a listener reading the database finds it
    private void publish(DataAction action, Object data) {
        if (!redisNotifier.enabled()) return;
        if (DataProxyContext.params().length == 0 || null == DataProxyContext.params()[0]) throw new EruptWebApiRuntimeException("DataProxy params[0] not found → redis channel");
        redisNotifier.publish(DataProxyContext.params()[0], new NotifyData<>(action, data));
    }

}
