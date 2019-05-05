package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.model.BoolAndReason;

import javax.transaction.Transactional;
import java.io.File;
import java.io.InputStream;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface DataProxy<MODEL> {

    //改
    default BoolAndReason beforeEdit(MODEL o) {
        return new BoolAndReason(true, null);
    }

    default void afterEdit(MODEL o) {
    }

    //增
    default BoolAndReason beforeAdd(MODEL o) {
        return this.beforeEdit(o);
    }

    default void afterAdd(MODEL o) {
        this.afterEdit(o);
    }

    //删
    default BoolAndReason beforeDelete(MODEL o) {
        return new BoolAndReason(true, null);
    }

    default void afterDelete(MODEL o) {
    }

    //查
    default void beforeFetch(JsonObject condtion) {

    }

    default void afterFetch(Object o) {
    }

    //文件上传
    default BoolAndReason upLoadFile(InputStream inputStream, File file) {
        return new BoolAndReason(true, null);
    }

}
