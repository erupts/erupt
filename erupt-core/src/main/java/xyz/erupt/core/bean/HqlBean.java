package xyz.erupt.core.bean;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liyuepeng
 * @date 2018-12-26.
 */
@Getter
@Setter
public class HqlBean {

    //后台自定义参数
    private String customCondition;
    //前台条件参数（仅注解声明为查询条件时条件才有效）
    private JsonObject searchCondition;

    private String cols;

    private boolean countSql;

    public HqlBean(String cols, String customCondition, JsonObject searchCondition, String orderBy, boolean countSql) {
        this.cols = cols;
        this.customCondition = customCondition;
        this.searchCondition = searchCondition;
        this.orderBy = orderBy;
        this.countSql = countSql;
    }

    public HqlBean(String cols) {
        this.cols = cols;
    }

    private String orderBy;
}
