package com.erupt.annotation.fun;

import com.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface OperationHandler {

    BoolAndReason exec(Object keys, Object param);
}
