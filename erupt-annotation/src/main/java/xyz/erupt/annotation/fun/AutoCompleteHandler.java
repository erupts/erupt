package xyz.erupt.annotation.fun;

import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-08-14
 */
public interface AutoCompleteHandler {

    /**
     * @param val   前端输入值
     * @param param 注解回传参数
     * @return
     */
    List<Object> completeHandler(String val, String[] param);
}
