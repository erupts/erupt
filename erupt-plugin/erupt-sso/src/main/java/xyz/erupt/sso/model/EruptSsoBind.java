package xyz.erupt.sso.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptUser;

import java.time.LocalDateTime;

/**
 * Which external identity an erupt user signs in with.
 *
 * <p>The provider's subject is what the binding keys on, never the account name or the mail
 * address: those are renamed and reused, the subject is not. A claim only ever matches an
 * existing account once, when the binding is first created.
 *
 * <p>The binding also keeps what other modules need to reach the user at the provider
 * afterwards: the messaging identifier ({@link #openId}, the claim the provider row maps,
 * unlike {@link #subject} which the login flow picks itself and nobody edits) and the raw
 * user info of the last login ({@link #claims}), so a provider specific field is available
 * without a schema change. Both are rewritten on every login. See
 * {@code EruptSsoBindService} for the lookup API.
 *
 * @author YuePeng
 * date 2026-09-18
 */
@Entity
@Table(name = "e_upms_sso_bind", uniqueConstraints = @UniqueConstraint(name = "uk_sso_bind", columnNames = {"sso_id", "subject"}))
@Erupt(
        name = "SSO Binding",
        // bindings are created by signing in, an admin may only look at them and revoke one
        power = @Power(add = false, edit = false, importable = false, cellEdit = false)
)
@EruptI18n
@Getter
@Setter
public class EruptSsoBind extends MetaModelUpdateVo {

    @ManyToOne
    @EruptField(
            views = @View(title = "Provider", column = "name"),
            edit = @Edit(title = "Provider", type = EditType.REFERENCE_TABLE, search = @Search)
    )
    private EruptSso sso;

    @ManyToOne
    @EruptField(
            views = @View(title = "User", column = "account"),
            edit = @Edit(title = "User", type = EditType.REFERENCE_TABLE, search = @Search)
    )
    private EruptUser eruptUser;

    @Column(length = 255, nullable = false)
    @EruptField(
            views = @View(title = "Subject", sortable = true)
    )
    private String subject;

    @Column(length = 255)
    @EruptField(
            views = @View(title = "Open ID", sortable = true)
    )
    private String openId;

    @EruptField(
            views = @View(title = "Last Login", sortable = true, type = ViewType.DATE_TIME)
    )
    private LocalDateTime lastLoginTime;

    // The user info document as the provider sent it, envelope removed; a snapshot, not
    // a source of truth, so a JSON column rather than a column per field
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Claims", type = ViewType.CODE, width = "90px"),
            edit = @Edit(title = "Claims", codeEditType = @CodeEditorType(language = "json"))
    )
    private String claims;

}
