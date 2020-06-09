package xyz.demo.erupt.example;

import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author liyuepeng
 * @date 2020-03-06
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(StreamUtils.copyToString(Main.class.getResourceAsStream("/application.yml"), Charset.forName("utf-8")));
        System.out.println(new File(Main.class.getResource("/application.yml").getFile()).toString());
    }
}
