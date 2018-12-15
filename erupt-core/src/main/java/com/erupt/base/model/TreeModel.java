package com.erupt.base.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by liyuepeng on 11/15/18.
 */
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

    public Collection<TreeModel> getChildren() {
        return children;
    }

    public void setChildren(Collection<TreeModel> children) {
        this.children = children;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
