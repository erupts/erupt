package xyz.erupt.core.model;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by liyuepeng on 2018-12-26.
 */
@Getter
@Setter
public class HqlModel {

    //后台自定义参数
    private String customerCondition;
    //前台条件参数（仅注解声明为查询条件时条件才有效）
    private JsonObject searchCondition;

    private String cols;

    public HqlModel(String cols, String customerCondition, JsonObject searchCondition, String orderBy) {
        this.cols = cols;
        this.customerCondition = customerCondition;
        this.searchCondition = searchCondition;
        this.orderBy = orderBy;
    }

    public HqlModel(String cols) {
        this.cols = cols;
    }

    private String orderBy;
}
