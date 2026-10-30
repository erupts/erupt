package xyz.erupt.share.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The session a visitor gets for a link, and the frontend route that renders its target.
 *
 * @author YuePeng
 */
@Getter
@AllArgsConstructor
public class ShareSessionVo {

    private String token;

    private String route;

    // Minutes until the session ends
    private int expireMinutes;

}
