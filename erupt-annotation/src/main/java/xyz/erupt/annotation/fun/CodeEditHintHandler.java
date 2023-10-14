package xyz.erupt.annotation.fun;

import java.util.List;

/**
 * @author YuePeng
 * date 2023/8/24 20:26
 */
public interface CodeEditHintHandler {

    //fetch hint list
    List<String> hint(String[] params);

}
