package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public class DataProxy {

    //改
    public BoolAndReason beforeEdit(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterEdit(Object o) {

    }

    //增
    public BoolAndReason beforeAdd(Object o) {
        return this.beforeEdit(o);
    }

    public void afterAdd(Object o) {
        this.afterEdit(o);
    }

    //删
    public BoolAndReason beforeDelete(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterDelete(Object o) {

    }

    //查
    public BoolAndReason beforeFetch(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterFetch(Object o) {

    }

}
