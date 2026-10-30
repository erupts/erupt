package xyz.erupt.share.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * What a share session may reach, kept in the session store next to the token.
 *
 * @author YuePeng
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShareScope {

    private String code;

    private String type;

    // FORM: the erupt whose form the visitor fills
    private String erupt;

    // PAGE: the template path the visitor reads
    private String path;

}
