package xyz.erupt.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author YuePeng
 * date 2021/3/28 18:51
 */
@Configuration
@RestController
public class EruptWebConfig {

    private final String passport;

    public EruptWebConfig() throws IOException {
        try (InputStream input = EruptWebConfig.class.getClassLoader().getResourceAsStream("public/index.html")) {
            if (null == input) throw new RuntimeException("erupt-web resources not found");
            this.passport = StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        }
    }

    @GetMapping(value = {"/", "/index.html"}, produces = {"text/html;charset=utf-8"})
    public String index(HttpServletResponse response) {
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
        return passport;
    }

}
