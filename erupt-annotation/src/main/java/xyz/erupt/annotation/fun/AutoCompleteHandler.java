package xyz.erupt.annotation.fun;

import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-08-14
 */
public interface AutoCompleteHandler {

    List<Object> completeHandler(String val, String[] param);
}
