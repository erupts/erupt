package xyz.erupt.annotation.fun;

import java.util.HashMap;
import java.util.Map;

public interface VLEnum {

    Map<String, String> map = new HashMap<>();

    default void setVL(String value, String label) {
        map.put(value, label);
    }

}
