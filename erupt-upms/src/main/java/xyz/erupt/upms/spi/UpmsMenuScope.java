package xyz.erupt.upms.spi;

import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;

import java.util.Set;

/**
 * Narrows the menus a user may ever hold, beyond what roles grant. Menus are shared definitions
 * rather than tenant rows, so a multi-tenant deployment registers one implementation that limits
 * a tenant's users to the menus its subscription includes; it applies to the menu tree a session
 * receives and to the menus a role editor may pick.
 *
 * @author YuePeng
 */
public interface UpmsMenuScope {

    /**
     * @return ids of the menus available to the user, or {@code null} when nothing narrows them
     */
    Set<Long> menuIds(EruptUser user);

    default boolean allows(EruptUser user, EruptMenu menu) {
        Set<Long> ids = this.menuIds(user);
        return null == ids || ids.contains(menu.getId());
    }
}
