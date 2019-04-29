package xyz.erupt.core.model;

import lombok.Data;

import java.util.Collection;

/**
 * Created by liyuepeng on 11/15/18.
 */
@Data
public class TreeModel {

    private Collection<TreeModel> children;

    private String id;

    private String label;

    private String pid;

    private Object data;

    public TreeModel(Object id, Object label, Object pid, Object data) {
        if (id != null) {
            this.id = id.toString();
        }
        if (label != null) {
            this.label = label.toString();
        }
        if (pid != null) {
            this.pid = pid.toString();
        }
        this.data = data;
    }
}
