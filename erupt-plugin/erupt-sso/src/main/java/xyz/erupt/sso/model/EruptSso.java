package xyz.erupt.sso.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.DragSort;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptRole;
import xyz.erupt.sso.model.data_proxy.EruptSsoDataProxy;

import java.util.Set;

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
        layout = @Layout(formSteps = true),
        dragSort = @DragSort(field = "sort")
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

    private Integer sort;

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
                    desc = "Where the provider sends the browser back, must be registered there verbatim; "
                            + "empty means http(s)://<this host>/erupt-api/sso/callback/<code>, fill it only behind a proxy or a different public domain",
                    inputType = @InputType(fullSpan = true))
    )
    private String redirectUri;

    @Transient
    @EruptField(edit = @Edit(title = "Client", type = EditType.DIVIDE))
    private String clientStep;

    @Column(nullable = false)
    @EruptField(
            views = @View(title = "Client ID"),
            edit = @Edit(title = "Client ID", notNull = true, cellEdit = false, inputType = @InputType(fullSpan = true))
    )
    private String clientId;

    // No @View: a client secret is write only, the framework masks the form value
    // and restores the stored one when the mask comes back unchanged
    @Column(length = 512)
    @EruptField(
            edit = @Edit(title = "Client Secret", notNull = true, type = EditType.PASSWORD, inputType = @InputType(fullSpan = true))
    )
    private String clientSecret;

    @Column(length = 255)
    @EruptField(
            // A space is what the authorization request wants, so it is what gets stored:
            // the tags join and split on it and no conversion sits in between. The presets
            // are the OIDC standard scopes; anything a provider invents is typed in.
            edit = @Edit(title = "Scopes", notNull = true, type = EditType.TAGS,
                    tagsType = @TagsType(joinSeparator = " ", allowExtension = true,
                            tags = {"openid", "profile", "email", "phone", "address", "groups"}))
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

    @Column(length = 64)
    @EruptField(
            edit = @Edit(title = "Phone Claim", desc = "OIDC: phone_number, Feishu: mobile")
    )
    private String phoneClaim;

    @Column(length = 64)
    @EruptField(
            edit = @Edit(title = "Avatar Claim", desc = "URL of the picture; OIDC: picture, Feishu: avatar_url")
    )
    private String avatarClaim;

    @ManyToMany
    @JoinTable(
            name = "e_upms_sso_role",
            joinColumns = @JoinColumn(name = "sso_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            foreignKey = @ForeignKey(name = "fk_sso_role_sso"),
            inverseForeignKey = @ForeignKey(name = "fk_sso_role_role")
    )
    @OrderBy
    @EruptField(
            views = @View(title = "Default Roles"),
            edit = @Edit(title = "Default Roles", type = EditType.CHECKBOX,
                    desc = "Given to a user the first time this provider creates it")
    )
    private Set<EruptRole> defaultRoles;

    @EruptField(
            views = @View(title = "Sync Profile", sortable = true),
            edit = @Edit(title = "Sync Profile", type = EditType.BOOLEAN, notNull = true, cellEdit = false,
                    desc = "Refresh name, email, phone and avatar from the provider on every login; off only fills empty fields",
                    boolType = @BoolType(trueText = "Every login", falseText = "Fill empty only"))
    )
    private Boolean syncProfile = true;

    @EruptField(
            views = @View(title = "Auto Create", sortable = true),
            edit = @Edit(title = "Auto Create", type = EditType.BOOLEAN, notNull = true, cellEdit = false,
                    desc = "Create an erupt user the first time an unknown identity signs in",
                    boolType = @BoolType(trueText = "Create", falseText = "Reject"))
    )
    private Boolean autoCreate = true;

    @EruptField(
            views = @View(title = "Grant Roles On Login", sortable = true),
            edit = @Edit(title = "Grant Roles On Login", type = EditType.BOOLEAN, notNull = true, cellEdit = false,
                    desc = "Also add any default role a bound user is missing on each login; roles are only ever added, never taken away")
    )
    private Boolean grantRolesOnLogin = false;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
