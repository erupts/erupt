package xyz.erupt.core.constant;

/**
 * @author YuePeng
 * date 2020-06-09
 */
public class EruptConst {

    public static final String ERUPT = "erupt";

    public static final String BASE_PACKAGE = "xyz.erupt";

    public static final String ERUPT_DIR = ".erupt";

    // Absolute path of the .erupt directory under the process working directory
    public static final String ERUPT_DIR_PATH = System.getProperty("user.dir") + java.io.File.separator + ERUPT_DIR;

    public static final String DEFAULT_DATA_PROCESSOR = "JPA";

    public static final String DOT = ".";

    public static final String ERUPT_LOG = "erupt-log";

    public static final String AN = "abcdef0123456789";

    /**
     * Sentinel value returned to the client instead of stored PASSWORD field values.
     * When an edit form is submitted with this exact value, the stored value is preserved.
     */
    public static final String PASSWORD_PLACEHOLDER = "••••••";

}
