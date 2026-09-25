package xyz.erupt.upms.base;

import lombok.Getter;
import lombok.Setter;

/**
 * Self-service profile fields a signed-in user may change about themselves.
 * Deliberately narrow: nothing here touches roles, status or contact channels.
 *
 * @author YuePeng
 * date 2026-09-24.
 */
@Getter
@Setter
public class ProfileBody {

    private String name;

    private String avatar;

}
