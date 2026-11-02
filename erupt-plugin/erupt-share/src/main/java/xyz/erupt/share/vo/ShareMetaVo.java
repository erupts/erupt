package xyz.erupt.share.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * What the landing page shows before the visitor opens the link.
 *
 * @author YuePeng
 */
@Getter
@Builder
public class ShareMetaVo {

    private String title;

    private String type;

    private boolean requirePassword;

    // Name of whoever issued the link, shown so the visitor knows who is asking
    private String issuer;

    // False when the link cannot be opened any more; reason says why, translated
    private boolean available;

    private String reason;

}
