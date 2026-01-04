package xyz.erupt.sample.model;

import org.apache.commons.collections4.map.HashedMap;
import xyz.erupt.annotation.sub_field.sub_edit.OnChange;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Map;

public class DemoInputChange implements OnChange<Demo> {
    @Override
    public Map<String, Object> populateForm(Demo demo, String[] params) {
        Map<String, Object> map = new HashedMap<>();
        map.put(LambdaSee.field(Demo::getInput2), demo.getInput());
        return map;
    }

    @Override
    public Map<String, String> buildEditExpr(Demo demo, String[] params) {
        return Map.of(LambdaSee.field(Demo::getInput2),
                "edit.title = '" + demo.getInput() + "';" +
                    "edit.desc = '" + demo.getInput() + "';");
    }
}
