package xyz.erupt.decision.constant;

/**
 * @author YuePeng
 */
public class DecisionConst {

    // Path segment of the decision REST API, under /erupt-api
    public static final String API = "/decision";

    // Question id generated for a question asked from java code, suffixed with its position
    public static final String GENERATED_ID = "q";

    // A Choice may not exceed this many options, and a Score this many levels
    public static final int MAX_CHOICE_OPTIONS = 255;

    public static final int MAX_SCORE_LEVELS = 10;

    public static final int MIN_SCORE_LEVELS = 2;

}
