package xyz.erupt.core.util;

import xyz.erupt.core.controller.EruptApi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2021/3/28 17:45
 */
public class EruptVersionUtil {

    private static Properties props;

    static {
        String path = "/erupt-core.properties";
        Properties props = new Properties();
        try (InputStream stream = EruptApi.class.getResourceAsStream(path)) {
            props.load(stream);
            EruptVersionUtil.props = props;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getEruptVersion() {
        return props.getProperty("version");
    }


}
