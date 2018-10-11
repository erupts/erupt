package com.erupt.annotation.fun;

/**
 * Created by liyuepeng on 10/9/18.
 */
public interface DataProxy {

    boolean beforeSave();

    void afterSave();

    boolean beforeDelete();

    void afterDelete();

    boolean beforeFetch();

    void afterFetch();

}
