package xyz.erupt.test.model.edit;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.util.List;
import java.util.Map;

@Component
public class TestAutoCompleteHandler implements AutoCompleteHandler {

    @Override
    public List<Object> completeHandler(Map<String, Object> formData, String val, String[] param) {
        return List.of("option1", "option2", "option3");
    }
}
