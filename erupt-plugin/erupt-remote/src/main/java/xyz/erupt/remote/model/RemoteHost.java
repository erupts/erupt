package xyz.erupt.remote.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.OpenWay;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.remote.model.data_proxy.RemoteHostDataProxy;

/**
 * A remote machine that can be opened from the browser as a VNC desktop or an SSH terminal.
 *
 * @author YuePeng
 */
@Erupt(
        name = "Remote Host",
        dataProxy = RemoteHostDataProxy.class,
        orderBy = "id desc",
        rowOperation = @RowOperation(
                title = "Connect", icon = "fa fa-desktop",
                mode = RowOperation.Mode.SINGLE, type = RowOperation.Type.TPL,
                tpl = @Tpl(path = "/remote/{id}", openWay = OpenWay.ROUTER)
        )
)
@Table(name = "e_remote_host")
@Entity
@Getter
@Setter
@EruptI18n
public class RemoteHost extends MetaModelUpdateVo {

    /** Menu value of the auto-generated table menu; also the permission key for the remote APIs */
    public static final String MENU_VALUE = "RemoteHost";

    public static final String PROTOCOL_VNC = "VNC";

    public static final String PROTOCOL_SSH = "SSH";

    @EruptField(
            views = @View(title = "Host Name"),
            edit = @Edit(title = "Host Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @Column(length = 16)
    @EruptField(
            views = @View(title = "Protocol", width = "120px"),
            edit = @Edit(title = "Protocol", notNull = true, type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = PROTOCOL_VNC, label = "VNC (desktop)"),
                            @VL(value = PROTOCOL_SSH, label = "SSH (terminal)")
                    }))
    )
    private String protocol = PROTOCOL_VNC;

    @EruptField(
            views = @View(title = "Host"),
            edit = @Edit(title = "Host", notNull = true, desc = "IP address or hostname reachable from the erupt server",
                    search = @Search(operator = QueryExpression.LIKE))
    )
    private String host;

    @EruptField(
            views = @View(title = "Port", width = "80px"),
            edit = @Edit(title = "Port", notNull = true, type = EditType.NUMBER, desc = "5900 for VNC, 22 for SSH",
                    numberType = @NumberType(min = 1, max = 65535, precision = 0))
    )
    private Integer port = 5900;

    @EruptField(
            views = @View(title = "Username"),
            edit = @Edit(title = "Username", desc = "SSH login user",
                    dynamic = @Dynamic(dependField = "protocol", condition = "value === 'SSH'"))
    )
    private String username;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Password", type = EditType.PASSWORD,
                    desc = "VNC: server password (first 8 characters are used), leave empty to be prompted in the browser. SSH: login password, or the passphrase when a private key is set")
    )
    private String password;

    @Column(length = 8192)
    @EruptField(
            // a private key is pasted whole in the form, never nudged one cell at a time
            edit = @Edit(title = "Private Key", type = EditType.TEXTAREA, cellEdit = false,
                    desc = "PEM private key for SSH public-key authentication; takes precedence over the password",
                    dynamic = @Dynamic(dependField = "protocol", condition = "value === 'SSH'"))
    )
    private String privateKey;

    @EruptField(
            views = @View(title = "Enabled", width = "80px"),
            edit = @Edit(title = "Enabled", type = EditType.BOOLEAN, notNull = true, search = @Search, boolType = @BoolType)
    )
    private Boolean enabled = true;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark"),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
