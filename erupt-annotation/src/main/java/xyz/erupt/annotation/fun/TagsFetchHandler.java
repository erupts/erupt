package xyz.erupt.annotation.fun;

import java.util.List;

/**
 * @author YuePeng
 * date 2019-07-25.
 */
public interface TagsFetchHandler {

    List<String> fetchTags(String[] params);

}
