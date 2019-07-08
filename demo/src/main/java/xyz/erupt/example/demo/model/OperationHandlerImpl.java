package xyz.erupt.example.demo.model;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.core.util.ProjectUtil;

import java.util.List;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class OperationHandlerImpl implements OperationHandler<List<Test>> {
    @Override
    public BoolAndReason exec(List<Test> data, JsonObject param) {
        new ProjectUtil().projectStartLoaded("text_mmo", (isFirst) -> {
            if (isFirst) {
                System.out.println("first ");
            } else {
                System.out.println("不是第一次调用");
            }
        });
        return new BoolAndReason(false, "2333");
    }


    public static void main(String[] args) {
        new ProjectUtil().projectStartLoaded("demo", (bool) -> {
            if (bool) {
                System.out.println("first ");
            } else {
                System.out.println("不是第一次调用");
            }
        });
    }
}
