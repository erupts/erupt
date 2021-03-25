package xyz.erupt.core.view;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author YuePeng
 * date 2018-11-15.
 */
@Getter
@Setter
public class TreeModel {

    private Collection<TreeModel> children;

    private String id;

    private String label;

    private String pid;

    private boolean root;

    public TreeModel(Object id, Object label, Object pid, Object root) {
        this(id, label, pid);
        this.setRoot(root);
    }


    public TreeModel(Object id, Object label, Object pid) {
        if (id != null) {
            this.id = id.toString();
        }
        if (label != null) {
            this.label = label.toString();
        }
        if (pid != null) {
            this.pid = pid.toString();
        }
    }

    public void setRoot(Object root) {
        if (null != root && StringUtils.isNotBlank(root.toString())) {
            if (null == pid) {
                this.root = false;
            } else {
                this.root = root.equals(pid);
            }
        } else {
            this.root = null == pid;
        }
    }
}
