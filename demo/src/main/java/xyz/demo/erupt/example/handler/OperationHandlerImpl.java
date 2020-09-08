package xyz.demo.erupt.example.handler;

import xyz.demo.erupt.example.model.Demo;
import xyz.erupt.annotation.fun.OperationHandler;

/**
 * @author liyuepeng
 * @date 2018-10-10.
 */
public class OperationHandlerImpl implements OperationHandler<Demo, Void> {

    @Override
    public void exec(Demo data, Void void1, String[] param) {
        System.out.println(123);
//        throw new EruptApiErrorTip(new EruptApiModel(EruptApiModel.Status.SUCCESS,
//                "lalala", EruptApiModel.PromptWay.NOTIFY));
    }

}
