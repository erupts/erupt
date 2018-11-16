package com.erupt.annotation.fun;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface OperationHandler {

    boolean exce(Object o);
}
