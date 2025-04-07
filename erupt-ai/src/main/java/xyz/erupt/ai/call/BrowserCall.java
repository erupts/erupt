package xyz.erupt.ai.call;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import xyz.erupt.ai.annotation.AiFuncParam;

/**
 * @author YuePeng
 * date 2025/3/14 23:25
 */
@Component
public class BrowserCall implements AiFunctionCall {

    @AiFuncParam("路径信息")
    private String path = "/";

    @Override
    public String description() {
        return "快捷使用shell open命令，当用户输入网址或者文件路径时帮助用户快捷打开";
    }

    @Override
    @SneakyThrows
    public String call(String prompt) {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("open " + path);
        return "已打开: " + path;
    }

}
