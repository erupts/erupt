package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.model.PlaceholderData;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by liyuepeng on 11/5/18.
 */
@Transactional
public interface ConditionHandler {

    List<PlaceholderData> handler();
}
