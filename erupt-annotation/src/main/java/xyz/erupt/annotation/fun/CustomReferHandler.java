package xyz.erupt.annotation.fun;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by liyuepeng on 2019-02-20.
 */
@Transactional
public interface CustomReferHandler {

    List refer(Field field, String param);
}
