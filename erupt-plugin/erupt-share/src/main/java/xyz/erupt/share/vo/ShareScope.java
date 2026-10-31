package xyz.erupt.share.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.erupt.share.model.EruptShareLink;

/**
 * What a share session may reach, kept in the session store next to the token: the link, its
 * type, and the erupt of a form or the menu code of a page.
 *
 * @author YuePeng
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShareScope {

    private String code;

    private String type;

    private String target;

    public boolean isForm() {
        return EruptShareLink.TYPE_FORM.equals(type);
    }
}
