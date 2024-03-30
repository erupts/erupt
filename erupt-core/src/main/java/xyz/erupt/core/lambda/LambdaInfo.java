package xyz.erupt.core.lambda;

import lombok.Getter;

@Getter
public class LambdaInfo {

    private final Class<?> clazz;

    private final String method;

    private final String field;

    public LambdaInfo(Class<?> clazz, String method, String field) {
        this.clazz = clazz;
        this.method = method;
        this.field = field;
    }

}
