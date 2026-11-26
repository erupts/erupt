package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

@Component
public class TestOperationHandler implements OperationHandler<RowOperationModel, Void> {

    // the rows of the last call, for a test to see that the handler was reached and with what
    public static volatile List<RowOperationModel> lastRows;

    @Override
    public String exec(List<RowOperationModel> data, Void form, String[] param) {
        lastRows = data;
        // custom operation logic; return null means no frontend JS execution needed
        return null;
    }
}
