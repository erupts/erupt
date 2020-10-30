package xyz.erupt.core.query;

import lombok.Data;

@Data
public class Order {

    private String properties;

    private Direction direction;

    public enum Direction {
        ASC, DESC
    }
}
