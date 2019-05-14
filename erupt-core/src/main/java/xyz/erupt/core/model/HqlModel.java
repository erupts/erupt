package xyz.erupt.core.model;

import com.google.gson.JsonObject;
import lombok.Data;

import java.util.Map;

/**
 * Created by liyuepeng on 2018-12-26.
 */
@Data
public class HqlModel {

    public HqlModel(String cols, JsonObject condition, String orderBy) {
        this.cols = cols;
        this.eruptSearchCondition = condition;
        this.orderBy = orderBy;
    }

    public HqlModel(String cols, String customCondition, JsonObject eruptSearchCondition, String orderBy) {
        this.cols = cols;
        this.customCondition = customCondition;
        this.eruptSearchCondition = eruptSearchCondition;
        this.orderBy = orderBy;
    }

    private String cols;

    //后台自定义参数
    private String customCondition;

    //Erupt注解所允许的查询条件
    private JsonObject eruptSearchCondition;

    private String orderBy;
}
