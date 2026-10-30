package xyz.erupt.share.constant;

/**
 * Session store keys of the share module.
 *
 * @author YuePeng
 */
public class ShareSessionKey {

    private static final String SPACE = "erupt-share:";

    // The scope of a session minted for a share link, keyed by its token
    public static final String SCOPE = SPACE + "scope:";

    // Failed or repeated exchange attempts per client address
    public static final String ATTEMPT = SPACE + "attempt:";

}
