package xyz.erupt.core.constant;

/**
 * Service interaction constants
 * @author YuePeng
 * date 2022/1/13 00:33
 */
public class EruptMutualConst {

    public static final String ERUPT = EruptConst.ERUPT;

    public static final String USER = "user";

    public static final String TOKEN = "token";

    // The erupt whose menu authorizes a request for one of its nested erupts (sub-form, tab, reference)
    public static final String ERUPT_PARENT = "eruptParent";

    public static final String URL_PARAM_ERUPT_PARENT = "_" + ERUPT_PARENT;

    public static final String LANG = "Lang";

    // URL form of the language, the way _token mirrors the token header: the admin hands it to
    // every server-rendered tpl page, which cannot set a header on its own document request
    public static final String URL_PARAM_LANG = "_lang";

}
