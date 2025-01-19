package xyz.erupt.upms.service;

import org.springframework.stereotype.Component;
import xyz.erupt.core.cache.EruptCacheLRU;

/**
 * @author YuePeng
 * date 2024/12/19 23:28
 */
@Component
public class EruptLocalSession extends EruptCacheLRU<Object> {

    public EruptLocalSession() {
        super(100000);
    }

}
