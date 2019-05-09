package xyz.erupt.annotation.fun;

import com.google.gson.JsonObject;
import xyz.erupt.annotation.model.BoolAndReason;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface DataProxy<MODEL> {

    //改
    default BoolAndReason beforeEdit(MODEL o) {
        return new BoolAndReason(true, null);
    }

    default void afterEdit(MODEL o) {
    }

    //增
    default BoolAndReason beforeAdd(MODEL o) {
        return new BoolAndReason(true, null);
    }

    default void afterAdd(MODEL o) {
    }

    //删
    default BoolAndReason beforeDelete(Serializable id) {
        return new BoolAndReason(true, null);
    }

    default void afterDelete(Serializable id) {
    }

    //查
    default void beforeFetch(JsonObject condition) {
    }

    default void afterFetch(Collection list) {
    }

    //文件上传
    default BoolAndReason beforeUpLoadFile(InputStream inputStream, File file) {
        return new BoolAndReason(true, null);
    }

    default void afterUpLoadFile(File file, String relativePath) {
    }

}
