package xyz.erupt.core.dao;

import lombok.Data;

/**
 * Created by liyuepeng on 2019-01-13.
 */
@Data
public class NameAndAlias {
    private String name;

    private String alias;

    public NameAndAlias(String name, String alias) {
        this.name = name;
        if (name.contains(".")) {
            String[] aliasArr = alias.split("\\.");
            this.name = aliasArr[aliasArr.length - 1];
        } else {
            this.name = alias;
        }

        if (alias.contains(".")) {
            String[] aliasArr = alias.split("\\.");
            this.alias = aliasArr[aliasArr.length - 1];
        } else {
            this.alias = alias;
        }
    }
}
