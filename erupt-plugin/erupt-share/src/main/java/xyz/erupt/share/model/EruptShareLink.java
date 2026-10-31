package xyz.erupt.share.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.share.handler.ShareCopyLinkOperation;
import xyz.erupt.share.handler.ShareLinkProxy;
import xyz.erupt.share.handler.ShareTargetChoice;
import xyz.erupt.share.handler.ShareScopeFilter;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;
import xyz.erupt.upms.helper.UpmsTenant;

import java.util.Date;

/**
 * One public link. The code in the URL is the only secret: 22 random alphanumerics, never
 * reused. The row carries the issuer's tenant as a plain column rather than {@code @TenantId},
 * because a visitor opens the link before any tenant is known and the lookup has to find the
 * row from a platform session; {@link ShareScopeFilter} keeps the admin view tenant-bound.
 * <p>
 * The password, when set, is stored as a salted SHA-512 digest; the form takes it through a
 * transient field, so the digest never travels to the browser.
 *
 * @author YuePeng
 */
@EruptI18n
@Erupt(
        name = "Share Link",
        orderBy = "createTime desc",
        dataProxy = ShareLinkProxy.class,
        filter = @Filter(conditionHandler = ShareScopeFilter.class),
        power = @Power(export = false, importable = false),
        rowOperation = @RowOperation(code = ShareCopyLinkOperation.CODE, title = "Copy Link", icon = "fa fa-link",
                mode = RowOperation.Mode.SINGLE, callHint = "", operationHandler = ShareCopyLinkOperation.class)
)
@Entity
@Table(name = "e_share_link", indexes = @Index(columnList = "code", unique = true))
@Getter
@Setter
public class EruptShareLink extends HyperModelCreatorOnlyVo {

    public static final String TYPE_FORM = "FORM";

    public static final String TYPE_MENU = "MENU";

    public static final int CODE_LENGTH = 22;

    // Owner tenant, UpmsTenant.PLATFORM for the platform; stamped from the issuer's session
    @ColumnDefault("'" + UpmsTenant.PLATFORM + "'")
    @Column(length = AnnotationConst.CODE_LENGTH, nullable = false, updatable = false)
    private String tenantId;

    // The secret in the URL; the "Copy Link" row button turns it into the full address
    @Column(length = 32, nullable = false, updatable = false)
    @EruptField(
            views = @View(title = "Code", width = "200px"),
            edit = @Edit(title = "Code", show = false, readonly = @Readonly(allowChange = false))
    )
    private String code;

    @Column(length = 200, nullable = false)
    @EruptField(
            views = @View(title = "Title"),
            edit = @Edit(title = "Title", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String title;

    // A form anyone may fill, or a menu entry anyone may open as a user holding that menu alone would
    @Column(length = 10, nullable = false)
    @EruptField(
            views = @View(title = "Type", width = "110px"),
            edit = @Edit(title = "Type", notNull = true, search = @Search, type = EditType.CHOICE,
                    readonly = @Readonly(add = false),
                    choiceType = @ChoiceType(vl = {
                            @VL(value = TYPE_FORM, label = "Public Form"),
                            @VL(value = TYPE_MENU, label = "Public Page")
                    }))
    )
    private String type = TYPE_FORM;

    // FORM: the erupt whose add form the visitor fills; MENU: the code of the menu the visitor opens.
    // The options follow the type chosen above
    @Column(length = 100)
    @EruptField(
            views = @View(title = "Target", show = false),
            edit = @Edit(title = "Target", notNull = true, type = EditType.CHOICE, search = @Search,
                    readonly = @Readonly(add = false),
                    choiceType = @ChoiceType(fetchHandler = ShareTargetChoice.class, dependField = "type"))
    )
    private String target;

    // The target as the list names it: the model's display name, the menu's name; filled by ShareLinkProxy.afterFetch
    @Transient
    @EruptField(
            views = @View(title = "Target", width = "200px"),
            edit = @Edit(title = "Target", show = false, cellEdit = false)
    )
    private String targetName;

    @EruptField(
            views = @View(title = "Expire Time", sortable = true),
            edit = @Edit(title = "Expire Time", notNull = true, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date expireTime;

    @EruptField(
            views = @View(title = "Max Visits", width = "100px"),
            edit = @Edit(title = "Max Visits", desc = "Leave empty for unlimited")
    )
    private Integer maxVisits;

    // Both counted by the exchange in one update statement, never through the entity
    @Column(updatable = false)
    @EruptField(
            views = @View(title = "Visits", width = "80px"),
            edit = @Edit(title = "Visits", show = false, readonly = @Readonly(allowChange = false))
    )
    private Integer visitCount = 0;

    @Column(updatable = false)
    @EruptField(
            views = @View(title = "Last Visit", sortable = true),
            edit = @Edit(title = "Last Visit", show = false, readonly = @Readonly(allowChange = false), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date lastVisitTime;

    // Input only: the digest below is what the row keeps
    @Transient
    @EruptField(
            edit = @Edit(title = "Password", type = EditType.PASSWORD, desc = "Leave empty for no password; set again to change it")
    )
    private String password;

    @Column(length = 200)
    private String passwordHash;

    @EruptField(
            views = @View(title = "Enabled", width = "80px"),
            edit = @Edit(title = "Enabled", search = @Search)
    )
    private Boolean enabled = true;

    // Issuer identity frozen at creation: the visitor's session is opened in this name, from a
    // request that cannot read the issuer's user row (it may belong to another tenant)
    @Column(updatable = false)
    private Long issuerId;

    @Column(length = 100, updatable = false)
    private String issuerAccount;

    @Column(length = 100, updatable = false)
    private String issuerName;

    public boolean isForm() {
        return TYPE_FORM.equals(type);
    }
}
