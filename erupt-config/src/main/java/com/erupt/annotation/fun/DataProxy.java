package com.erupt.annotation.fun;

import com.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public class DataProxy {

    public BoolAndReason beforeSave(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterSave(Object o) {

    }

    public BoolAndReason beforeDelete(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterDelete(Object o) {

    }

    public BoolAndReason beforeFetch(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterFetch(Object o) {

    }

}
