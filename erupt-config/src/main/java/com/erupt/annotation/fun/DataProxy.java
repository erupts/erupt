package com.erupt.annotation.fun;

import com.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface DataProxy {

    BoolAndReason beforeSave(Object o);

    void afterSave(Object o);

    BoolAndReason beforeDelete(Object o);

    void afterDelete(Object o);

    BoolAndReason beforeFetch(Object o);

    void afterFetch(Object o);

}
