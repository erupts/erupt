package xyz.erupt.annotation.fun;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 11/5/18.
 */
@Transactional
public interface ConditionHandler {

    String handler(String condition);
}
