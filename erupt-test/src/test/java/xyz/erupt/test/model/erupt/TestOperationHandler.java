package xyz.erupt.test.model.erupt;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.OperationHandler;

import java.util.List;

@Component
public class TestOperationHandler implements OperationHandler<RowOperationModel, Void> {

    @Override
    public String exec(List<RowOperationModel> data, Void form, String[] param) {
        // 自定义操作逻辑，返回 null 表示无需前端执行 JS
        return null;
    }
}
