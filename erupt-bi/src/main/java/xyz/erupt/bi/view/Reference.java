package xyz.erupt.bi.view;

import lombok.Data;

import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-02-16
 */
@Data
public class Reference {

    Object key;

    Object title;

    List<Reference> children;

    public Reference(Object key, Object title) {
        this.key = key;
        this.title = title;
    }

    public Reference() {
    }
}
