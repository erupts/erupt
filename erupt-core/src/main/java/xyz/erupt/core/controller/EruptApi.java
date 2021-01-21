package xyz.erupt.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.EruptRestPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2020-04-22
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API)
public class EruptApi {

    private static Properties props;

    static {
        String path = "/erupt-core.properties";
        Properties props = new Properties();
        try (InputStream stream = EruptApi.class.getResourceAsStream(path)) {
            props.load(stream);
            EruptApi.props = props;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取当前Erupt版本号
    @GetMapping("/version")
    public String version() {
        return props.getProperty("version");
    }

    @GetMapping("/erupt-app")
    public String eruptApp() {
        return props.getProperty("version");
    }


}
