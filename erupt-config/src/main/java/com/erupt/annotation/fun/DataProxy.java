package com.erupt.annotation.fun;

import javax.transaction.Transactional;

/**
 * Created by liyuepeng on 10/9/18.
 */
@Transactional
public interface DataProxy {

    boolean beforeSave();

    void afterSave();

    boolean beforeDelete();

    void afterDelete();

    boolean beforeFetch();

    void afterFetch();

}
