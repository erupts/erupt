package xyz.erupt.i18n.constant;

/**
 * @author YuePeng
 * date 2021/9/13 00:44
 */
public class I18nConstant {

    public static final String VIEWS = "views";

    public static final String EDIT = "edit";

    public static final String TITLE = "title";

    private static final String DESC = "desc";

    private static final String ROW_OPERATION = "rowOperation";


    private static final String DRILLS = "drills";

    private static final String DOT = ".";

    public String[] eruptAnnI18nPath = new String[]{
            ROW_OPERATION + DOT + TITLE,
            ROW_OPERATION + DOT + "tip",
            DRILLS + DOT + TITLE
    };

    public String[] eruptFieldAnnI18nPath = new String[]{
            VIEWS + DOT + TITLE,
            VIEWS + DOT + DESC,
            EDIT + DOT + TITLE,
            EDIT + DOT + DESC,
            EDIT + DOT + "placeHolder",
    };

}
