package xyz.erupt.core.service;

import xyz.erupt.core.annotation.EruptScan;
import xyz.erupt.core.constant.EruptConst;

/**
 * @author liyuepeng
 * @date 2020-09-09
 */
public class EruptApplication {

    public static Class<?> primarySource;

    public static String[] scanPackage = {};

    public static void run(Class<?> primarySource, String... args) {
        EruptApplication.primarySource = primarySource;
        EruptScan eruptScan = primarySource.getAnnotation(EruptScan.class);
        if (null == eruptScan || eruptScan.value().length == 0) {
            scanPackage = new String[2];
            scanPackage[0] = primarySource.getPackage().getName();
            scanPackage[1] = EruptConst.BASE_PACKAGE;
        } else {
            scanPackage = eruptScan.value();
        }
    }

}
