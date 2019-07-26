package xyz.erupt.annotation.fun;

import javax.xml.ws.handler.Handler;
import java.util.Map;

/**
 * Created by liyuepeng on 2019-07-25.
 */
public interface FetchChoiceMapHandler extends Handler {

    Map<String, String> fetch(String[] params);
}
