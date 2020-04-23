package xyz.erupt.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.constant.RestPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author liyuepeng
 * @date 2020-04-22
 */
@RestController
@RequestMapping(RestPath.ERUPT_API)
public class EruptApi {

    private static Properties props;

    static {
        String path = "/core.properties";
        InputStream stream = EruptApi.class.getResourceAsStream(path);
        Properties props = new Properties();
        try {
            props.load(stream);
            EruptApi.props = props;
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/version")
    @ResponseBody
    public String version() {
        return props.getProperty("version");
    }
}
