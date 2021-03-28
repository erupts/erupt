package xyz.erupt.core.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {

    private String properties;

    private Direction direction;

    public enum Direction {
        ASC, DESC
    }
}
