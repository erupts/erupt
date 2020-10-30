package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * @author liyuepeng
 * @date 2018-11-15.
 */
@Getter
@Setter
public class TreeModel {

    private Collection<TreeModel> children;

    private String id;

    private String label;

    private String pid;

    private Object data;

    private boolean root = true;

    public TreeModel(Object id, Object label, Object pid, Object data, Object rootTag) {
        this(id, label, pid, data);
        if (null != rootTag) {
            this.root = Boolean.parseBoolean(rootTag.toString());
        }
    }

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

    public void setRoot(Object root) {
        if (null != root) {
            this.root = Boolean.parseBoolean(root.toString());
        }
    }
}
