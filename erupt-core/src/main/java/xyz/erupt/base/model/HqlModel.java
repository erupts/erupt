package xyz.erupt.base.model;

import com.google.gson.JsonObject;
import lombok.Data;

/**
 * Created by liyuepeng on 2018-12-26.
 */
@Data
public class HqlModel {

    public HqlModel(String cols) {
        this.cols = cols;
    }

    private String cols;

    private JsonObject condition;

    private String orderBy;


}
