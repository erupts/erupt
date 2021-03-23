package xyz.erupt.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import xyz.erupt.core.util.DateUtil;

/**
 * @author YuePeng
 * date 2021/3/1 13:03
 */
public class GsonFactory {

    private final static Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME)
            .setExclusionStrategies(new GsonExclusionStrategies())
            .serializeNulls().create();

    private GsonFactory() {
    }

    public static Gson getGson() {
        return gson;
    }
}
