package xyz.erupt.core.model;

import com.google.gson.JsonObject;
import lombok.Data;

/**
 * Created by liyuepeng on 2018-12-26.
 */
@Data
public class HqlModel {

    public HqlModel(String cols, JsonObject condition, String orderBy) {
        this.cols = cols;
        this.condition = condition;
        this.orderBy = orderBy;
    }

    public HqlModel(String cols, String customCondition, JsonObject condition, String orderBy) {
        this.cols = cols;
        this.customCondition = customCondition;
        this.condition = condition;
        this.orderBy = orderBy;
    }

    private String cols;

    //后台自定义参数
    private String customCondition;

    //接口参数
    private JsonObject condition;

    private String orderBy;


}
