package xyz.erupt.annotation.fun;

import xyz.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;
import java.io.InputStream;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public abstract class DataProxy<MODEL> {

    //改
    public BoolAndReason beforeEdit(MODEL o) {
        return new BoolAndReason(true, null);
    }

    public void afterEdit(MODEL o) {

    }

    //增
    public BoolAndReason beforeAdd(MODEL o) {
        return this.beforeEdit(o);
    }

    public void afterAdd(MODEL o) {
        this.afterEdit(o);
    }

    //删
    public BoolAndReason beforeDelete(MODEL o) {
        return new BoolAndReason(true, null);
    }

    public void afterDelete(MODEL o) {

    }

    //查
    public BoolAndReason beforeFetch(Object o) {
        return new BoolAndReason(true, null);
    }

    public void afterFetch(Object o) {

    }

    //文件上传
    public BoolAndReason upLoadFile(InputStream inputStream, String fileName) {
        return new BoolAndReason(true, null);
    }

}
