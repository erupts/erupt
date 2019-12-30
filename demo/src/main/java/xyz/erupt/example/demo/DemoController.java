package xyz.erupt.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liyuepeng
 * @date 2019-12-30
 */
@RestController
public class DemoController {

    @RequestMapping("/err")
    public void err() {
        throw new RuntimeException("xxxx");
    }
}
