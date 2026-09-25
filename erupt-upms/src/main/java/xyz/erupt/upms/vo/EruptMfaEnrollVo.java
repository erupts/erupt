package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * Everything the enrolment screen needs to render a QR code and a typed fallback.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Getter
@Setter
public class EruptMfaEnrollVo {

    //otpauth:// provisioning URI, rendered as a QR code by the client
    private String uri;

    //the same secret in groups of four, for users who cannot scan
    private String secret;

}
