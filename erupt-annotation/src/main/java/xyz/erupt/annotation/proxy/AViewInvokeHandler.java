package xyz.erupt.annotation.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author YuePeng
 * date 2022/2/5 13:47
 */
public class AViewInvokeHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(11);
        return null;
    }


}
