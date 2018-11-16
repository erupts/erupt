package com.erupt.annotation.util;

import com.erupt.annotation.fun.ConditionHandler;
import com.erupt.annotation.sub_erupt.Filter;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class ConfigUtil {

    public static String annoStrToJsonStr(String annotationStr) {
        String convertStr = annotationStr
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.\\w+", "")
                .replaceAll("class [a-zA-Z0-9.]+", "") //屏蔽类信息
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        return new JSONObject(convertStr).toString();
    }

    public static String switchFilterConditionToStr(Filter filter) {
        String conditionStr = filter.condition();
        if (filter.conditionHandlers().length > 0) {
            for (Class<? extends ConditionHandler> conditionHandler : filter.conditionHandlers()) {
                try {
                    ConditionHandler ch = conditionHandler.newInstance();

                    String placeHolderStr = Matcher.quoteReplacement(ch.placeHolderStr());
                    conditionStr = filter.condition().replaceAll("@" + placeHolderStr + "@",
                            Matcher.quoteReplacement(ch.placeHolderData()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        if (conditionStr.startsWith("'")) {
            conditionStr = conditionStr.substring(1, conditionStr.length() - 1);
        }
        return conditionStr;
    }


}
