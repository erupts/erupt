package xyz.erupt.core.view;

import lombok.Data;

import java.util.Collection;

/**
 * @author liyuepeng
 * @date 2018-11-15.
 */
@Data
public class TreeModel {

    private Collection<TreeModel> children;

    private String id;

    private String label;

    private String pid;

    private Object data;

    private transient Object rootTag;

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
