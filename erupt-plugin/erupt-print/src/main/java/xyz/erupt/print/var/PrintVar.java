package xyz.erupt.print.var;

import java.util.HashMap;
import java.util.Map;

/**
 * Print variable interface
 */
public abstract class PrintVar {

    public static final Map<String, PrintVar> PRINT_VAR_MAP = new HashMap<>();

    public PrintVar() {
        PRINT_VAR_MAP.put(this.code(), this);
    }

    public String code() {
        return this.getClass().getSimpleName();
    }

    public abstract String name();

    public abstract Object value();

}
