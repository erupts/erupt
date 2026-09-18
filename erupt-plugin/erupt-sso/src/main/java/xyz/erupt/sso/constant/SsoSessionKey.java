package xyz.erupt.sso.constant;

/**
 * @author YuePeng
 * date 2026-09-18
 */
public class SsoSessionKey {

    private static final String SSO_SPACE = "erupt-sso:";

    //Pending sign-on redirect, holds the provider and the PKCE verifier
    public static final String SSO_STATE = SSO_SPACE + "state:";

    //Session minted by a sign-on callback, waiting to be collected
    public static final String SSO_TICKET = SSO_SPACE + "ticket:";

}
