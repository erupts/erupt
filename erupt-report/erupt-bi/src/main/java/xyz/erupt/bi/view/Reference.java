package xyz.erupt.bi.view;

import lombok.Data;

/**
 * @author liyuepeng
 * @date 2020-02-16
 */
@Data
public class Reference {

    Object id;

    Object title;

    Object pid;

    public Reference(Object id, Object title) {
        this.id = id;
        this.title = title;
    }

    public Reference(Object id, Object title, Object pid) {
        this.id = id;
        this.title = title;
        this.pid = pid;
    }

    public Reference() {
    }
}
