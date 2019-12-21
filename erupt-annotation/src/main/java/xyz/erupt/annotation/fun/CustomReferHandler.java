package xyz.erupt.annotation.fun;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by liyuepeng on 2019-02-20.
 */
public interface CustomReferHandler {

    List<Object> refer(Field field, String... params);
}
