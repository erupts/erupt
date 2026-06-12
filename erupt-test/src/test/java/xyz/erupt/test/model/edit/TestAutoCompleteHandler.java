package xyz.erupt.test.model.edit;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.AutoCompleteHandler;

import java.util.List;

@Component
public class TestAutoCompleteHandler implements AutoCompleteHandler<AutoCompleteModel> {

    @Override
    public List<Object> completeHandler(AutoCompleteModel autoCompleteModel, String val, String[] param) {
        return List.of("option1", "option2", "option3");
    }

}
