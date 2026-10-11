package xyz.erupt.upms.spi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A user as a module shows it: who raised a flow, who approved, who commented. Carries only what a
 * UI renders, so a module can attach it to its own rows regardless of which user table it came from.
 *
 * @author YuePeng
 */
@Getter
@Setter
@NoArgsConstructor
public class UpmsUser {

    private Long id;

    private String name;

    private String avatar;

    private Boolean status;

    public UpmsUser(Long id) {
        this.id = id;
    }

    public UpmsUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public UpmsUser(Long id, String name, String avatar, Boolean status) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.status = status;
    }

}
