package com.erupt.annotation.util;

/**
 * Created by liyuepeng on 10/10/18.
 */
public class ConfigUtil {

    public static String annoStrToJsonStr(String annotationStr) {
        return annotationStr
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
                .replaceAll("@com\\.erupt\\.annotation\\.\\w+", "")
                .replace("=,", "='',")
                .replace("=)", "='')")
                .replace("=", ":")
                .replace("(", "{")
                .replace(")", "}");
    }



}
