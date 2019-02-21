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
        this.condition = condition;
        this.orderBy = orderBy;
    }

    private String cols;

    private JsonObject condition;

    private String orderBy;


}
