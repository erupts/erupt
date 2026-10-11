package xyz.erupt.upms.spi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * How to reach a user: the account a message can be addressed to and the contact details a
 * channel may need. Kept apart from {@link UpmsUser} so pages that only display a user never
 * carry contact data.
 *
 * @author YuePeng
 */
@Getter
@Setter
@NoArgsConstructor
public class UpmsUserContact extends UpmsUser {

    private String tenantId;

    private String account;

    private String email;

    private String phone;

    public UpmsUserContact(String tenantId, Long id, String account, String name) {
        super(id, name);
        this.tenantId = tenantId;
        this.account = account;
    }

}
