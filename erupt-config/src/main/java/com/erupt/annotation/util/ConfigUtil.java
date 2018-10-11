package com.erupt.annotation.util;

import org.json.JSONObject;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class ConfigUtil {

    public static String annoStrToJsonStr(String annotationStr) {
        String converVal = annotationStr
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.\\w+", "")
                .replaceAll("class [a-zA-Z0-9.]+", "") //屏蔽类信息
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
        //如此不严谨的JSON串也就只有org.json可以解析的出来
        return new JSONObject(converVal).toString();

    }


}
