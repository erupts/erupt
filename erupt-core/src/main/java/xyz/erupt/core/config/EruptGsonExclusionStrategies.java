package xyz.erupt.core.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import lombok.SneakyThrows;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.ReflectUtil;

import java.lang.reflect.Field;

/**
 * @author YuePeng
 * date 2021/1/13 00:09
 */
public class EruptGsonExclusionStrategies implements ExclusionStrategy {

    @Override
    @SneakyThrows
    public boolean shouldSkipField(FieldAttributes f) {
        MetaErupt metaErupt = MetaContext.getErupt();
        if (null == metaErupt || null == metaErupt.getName()) return false;
        if (null == f.getAnnotation(EruptSmartSkipSerialize.class)) return false;
        Class<?> currEruptClass = EruptCoreService.getErupt(metaErupt.getName()).getClazz();
        if (f.getDeclaringClass().isAssignableFrom(currEruptClass)) {
            Field ff = ReflectUtil.findClassField(currEruptClass, f.getName());
            if (null == ff) return false;
            return !f.getDeclaringClass().getName().equals(ff.getDeclaringClass().getName());
        } else {
            return true;
        }
    }

    @Override
    public boolean shouldSkipClass(Class<?> incomingClass) {
        return false;
    }

}
