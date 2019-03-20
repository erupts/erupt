package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface OperationHandler {

    BoolAndReason exec(Object data, Object param);
}
