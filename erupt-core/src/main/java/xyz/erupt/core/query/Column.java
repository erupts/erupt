package xyz.erupt.core.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Column {
    String name;

    String alias;

    public Column(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

}
