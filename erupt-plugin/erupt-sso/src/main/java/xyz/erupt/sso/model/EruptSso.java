package xyz.erupt.sso.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.sso.model.data_proxy.EruptSsoDataProxy;

/**
 * An external identity provider erupt delegates login to, over OAuth2 authorization code.
 *
 * <p>Both OIDC providers (Keycloak, Authing, Okta, Feishu, WeCom) and plain OAuth2 ones
 * (GitHub, Gitee) fit the same row: erupt never reads the id_token, it asks the user info
 * endpoint who the caller is. That keeps one flow for every provider and keeps a JWT
 * library out of the dependency tree.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Entity
@Table(name = "e_upms_sso")
@Erupt(
        name = "SSO Provider",
        orderBy = "EruptSso.sort asc",
        dataProxy = EruptSsoDataProxy.class,
        layout = @Layout(formSteps = true)
)
@EruptI18n
@Getter
@Setter
public class EruptSso extends MetaModelUpdateVo {

    @Transient
    @EruptField(edit = @Edit(title = "Basic", type = EditType.DIVIDE))
    private String basicStep;

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true, nullable = false)
    @EruptField(
            views = @View(title = "Code", sortable = true),
            // the code is part of the callback URL registered at the provider, so it is
            // granted once and never edited: changing it would silently break the callback
            edit = @Edit(title = "Code", desc = "Used in the callback URL, letters and digits only",
                    notNull = true, cellEdit = false, readonly = @Readonly(add = false),
                    search = @Search, inputType = @InputType(regex = "^[A-Za-z0-9_-]{2,32}$"))
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", desc = "Label of the button on the login page",
                    notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @EruptField(
            views = @View(title = "Icon", width = "70px"),
            edit = @Edit(title = "Icon", type = EditType.ICON, desc = "Refer to Font Awesome icon library")
    )
    private String icon;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", type = EditType.BOOLEAN, notNull = true, search = @Search,
                    boolType = @BoolType(trueText = "Enabled", falseText = "Disabled"))
    )
    private Boolean status = true;

    @EruptField(
            views = @View(title = "Sort", sortable = true),
            edit = @Edit(title = "Sort", notNull = true)
    )
    private Integer sort = 0;

    @Transient
    @EruptField(edit = @Edit(title = "Endpoint", type = EditType.DIVIDE))
    private String endpointStep;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Issuer", desc = "OIDC issuer; endpoints below are discovered from it when left empty",
                    inputType = @InputType(fullSpan = true))
    )
    private String issuer;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Authorize URL", inputType = @InputType(fullSpan = true))
    )
    private String authorizeUrl;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Token URL", inputType = @InputType(fullSpan = true))
    )
    private String tokenUrl;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "User Info URL", inputType = @InputType(fullSpan = true))
    )
    private String userInfoUrl;

    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Redirect URI",
                    desc = "Register this at the provider; derived from the request when left empty",
                    inputType = @InputType(fullSpan = true))
    )
    private String redirectUri;

    @Transient
    @EruptField(edit = @Edit(title = "Client", type = EditType.DIVIDE))
    private String clientStep;

    @Column(length = 255, nullable = false)
    @EruptField(
            views = @View(title = "Client ID"),
            edit = @Edit(title = "Client ID", notNull = true, cellEdit = false, inputType = @InputType(fullSpan = true))
    )
    private String clientId;

    // No @View: a client secret is write only, the framework masks the form value
    // and restores the stored one when the mask comes back unchanged
    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Client Secret", notNull = true, type = EditType.PASSWORD)
    )
    private String clientSecret;

    @Column(length = 255)
    @EruptField(
            edit = @Edit(title = "Scopes", desc = "Space separated", notNull = true, inputType = @InputType(fullSpan = true))
    )
    private String scopes = "openid profile email";

    @Transient
    @EruptField(edit = @Edit(title = "User Mapping", type = EditType.DIVIDE))
    private String mappingStep;

    @Column(length = 64)
    @EruptField(
            edit = @Edit(title = "Account Claim", notNull = true,
                    desc = "Claim matched against an erupt account on first login, e.g. preferred_username")
    )
    private String accountClaim = "preferred_username";

    @Column(length = 64)
    @EruptField(
            edit = @Edit(title = "Name Claim", notNull = true)
    )
    private String nameClaim = "name";

    @Column(length = 64)
    @EruptField(
            edit = @Edit(title = "Email Claim", notNull = true)
    )
    private String emailClaim = "email";

    @EruptField(
            views = @View(title = "Auto Create", sortable = true),
            edit = @Edit(title = "Auto Create", type = EditType.BOOLEAN, notNull = true, cellEdit = false,
                    desc = "Create an erupt user the first time an unknown identity signs in",
                    boolType = @BoolType(trueText = "Create", falseText = "Reject"))
    )
    private Boolean autoCreate = false;

    @ManyToOne
    @EruptField(
            views = @View(title = "Default Role", column = "name"),
            edit = @Edit(title = "Default Role", type = EditType.REFERENCE_TABLE,
                    desc = "Granted to auto created users only")
    )
    private EruptRole defaultRole;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
