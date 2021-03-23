package xyz.erupt.core.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import xyz.erupt.annotation.config.SkipSerialize;

/**
 * @author YuePeng
 * date 2021/1/13 00:09
 */
public class GsonExclusionStrategies implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        SkipSerialize skip = f.getAnnotation(SkipSerialize.class);
        return null != skip;
    }

    @Override
    public boolean shouldSkipClass(Class<?> incomingClass) {
        return false;
    }

}
