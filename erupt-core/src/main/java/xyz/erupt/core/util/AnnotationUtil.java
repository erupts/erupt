package xyz.erupt.core.util;

import xyz.erupt.annotation.fun.ConditionHandler;
import xyz.erupt.annotation.sub_erupt.Filter;

/**
 * Created by liyuepeng on 2019-02-28.
 */
public class AnnotationUtil {

    public static String switchFilterConditionToStr(Filter filter) {
        String condition = filter.condition();
        if (condition.startsWith("'")) {
            condition = condition.substring(1, condition.length() - 1);
        }
        if (filter.conditionHandlers().length > 0) {
            for (Class<? extends ConditionHandler> conditionHandler : filter.conditionHandlers()) {
                ConditionHandler ch = SpringUtil.getBean(conditionHandler);
                condition = ch.handler(condition);
            }
        }
        return condition;
    }

}
