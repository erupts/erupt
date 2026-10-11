package xyz.erupt.upms.spi;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * The organisational position of a user: organisation, post, roles, whether the user heads an
 * organisation. What a module needs to decide scope-based permissions.
 *
 * @author YuePeng
 */
@Getter
@Setter
public class UpmsUserDetail extends UpmsUser {

    private Long orgId;

    private Long postId;

    private Set<Long> roleIds;

    // true when the user heads at least one organisation
    private boolean orgHead;

}
