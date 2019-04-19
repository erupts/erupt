package xyz.erupt.core.util;

import org.json.JSONObject;
import xyz.erupt.annotation.fun.ConditionHandler;
import xyz.erupt.annotation.sub_erupt.Filter;

/**
 * Created by liyuepeng on 2019-02-28.
 */
public class AnnotationUtil {

    public static String annotationToJson(String annotationStr) {
        String convertStr = annotationStr
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
                .replaceAll("class [a-zA-Z0-9.]+", "") //屏蔽类信息
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        return new JSONObject(convertStr).toString();
    }

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
