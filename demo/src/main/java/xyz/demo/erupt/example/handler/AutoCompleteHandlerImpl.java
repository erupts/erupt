package xyz.demo.erupt.example.handler;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-08-14
 */
@Component
public class AutoCompleteHandlerImpl implements AutoCompleteHandler {

    @Override
    public List<Object> completeHandler(String val, String[] param) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(val + "-" + i);
        }
        return list;
    }
}
