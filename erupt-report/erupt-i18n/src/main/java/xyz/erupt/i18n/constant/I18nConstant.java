package xyz.erupt.i18n.constant;

/**
 * @author YuePeng
 * date 2021/9/13 00:44
 */
public class I18nConstant {

    public static final String VIEWS = "views";

    public static final String EDIT = "edit";

    public static final String TITLE = "title";

    public static final String TYPE = "type";

    public static final String DESC = "desc";

    public static final String ROW_OPERATION = "rowOperation";

    public static final String DRILLS = "drills";

    public static final String DOT = ".";

    public static final String TRUE_TEXT = "trueText";

    public static final String FALSE_TEXT = "falseText";

    public static final String BOOL_TYPE = "boolType";


    private static String[] eruptAnnI18nPath = new String[]{
            ROW_OPERATION + DOT + TITLE,
            ROW_OPERATION + DOT + "tip",
            DRILLS + DOT + TITLE
    };

    private static String[] eruptFieldAnnI18nPath = new String[]{
            VIEWS + DOT + TITLE,
            VIEWS + DOT + DESC,
            EDIT + DOT + TITLE,
            EDIT + DOT + DESC,
            EDIT + DOT + "placeHolder",
    };

    public static String[] getEruptAnnI18nPath() {
        return eruptAnnI18nPath;
    }

    public static String[] getEruptFieldAnnI18nPath() {
        return eruptFieldAnnI18nPath;
    }

}
