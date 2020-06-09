package xyz.demo.erupt.example.handler;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.view.EruptApiModel;

/**
 * @author liyuepeng
 * @date 2018-10-10.
 */
public class OperationHandlerImpl implements OperationHandler {

    @Override
    public void exec(Object data, JsonObject inputValue, String... param) {
        throw new EruptApiErrorTip(new EruptApiModel(EruptApiModel.Status.SUCCESS,
                "lalala", EruptApiModel.PromptWay.NOTIFY));
    }


//    public static void main(String[] args) {
//        new ProjectUtil().projectStartLoaded("demo", (bool) -> {
//            if (bool) {
//                System.out.println("first ");
//            } else {
//                System.out.println("不是第一次调用");
//            }
//        });
//    }
}
