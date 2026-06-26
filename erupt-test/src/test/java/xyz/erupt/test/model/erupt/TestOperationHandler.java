package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

@Component
public class TestOperationHandler implements OperationHandler<RowOperationModel, Void> {

    @Override
    public String exec(List<RowOperationModel> data, Void form, String[] param) {
        // custom operation logic; return null means no frontend JS execution needed
        return null;
    }
}
