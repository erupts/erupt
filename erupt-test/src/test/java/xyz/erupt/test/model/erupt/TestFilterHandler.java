package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.FilterHandler;

@Component
public class TestFilterHandler implements FilterHandler {

    @Override
    public String filter(String condition, String[] params) {
        // 动态生成过滤条件，如按当前用户隔离数据
        return "t.owner = 'admin'";
    }

}
