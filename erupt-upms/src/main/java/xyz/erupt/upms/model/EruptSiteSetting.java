package xyz.erupt.upms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.data_proxy.EruptSiteSettingDataProxy;

/**
 * Site settings an administrator edits at runtime: title, logos, favicon, login page and primary
 * color. A supplement to {@code app.js}, not a replacement: a value saved here wins over the same
 * key in {@code window.eruptSiteConfig}, a blank one leaves the {@code app.js} value in place, and
 * everything this form does not cover (map vendor, domain, tools...) stays in {@code app.js}. One
 * row, edited through a form view; delivered with {@code /erupt-app} before login, and a tenant's
 * own branding (erupt-tenant) still applies on top of it.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Entity
@Table(name = "e_upms_site_setting")
@Erupt(name = "Site Settings", dataProxy = EruptSiteSettingDataProxy.class)
@EruptI18n
@Getter
@Setter
public class EruptSiteSetting extends MetaModelUpdateVo {

    @Transient
    @EruptField(edit = @Edit(title = "Brand", desc = "Leave a field blank to keep the value from app.js", type = EditType.DIVIDE))
    private String brandSection;

    @EruptField(edit = @Edit(title = "Site Title", desc = "Browser tab and login page title"))
    private String title;

    @EruptField(edit = @Edit(title = "Site Description", desc = "Shown under the title on the login page"))
    private String description;

    @EruptField(edit = @Edit(title = "Logo Text", desc = "Shown next to the header logo; blank follows the title"))
    private String logoText;

    @EruptField(edit = @Edit(title = "Header Logo", type = EditType.ATTACHMENT,
            attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String logoPath;

    @EruptField(edit = @Edit(title = "Collapsed Logo", desc = "Shown when the sidebar is collapsed; blank follows the header logo",
            type = EditType.ATTACHMENT, attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String logoFoldPath;

    @EruptField(edit = @Edit(title = "Login Logo", desc = "Blank follows the header logo", type = EditType.ATTACHMENT,
            attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String loginLogoPath;

    @EruptField(edit = @Edit(title = "Favicon", desc = "Browser tab icon: ico, png or svg", type = EditType.ATTACHMENT,
            attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String faviconPath;

    @Transient
    @EruptField(edit = @Edit(title = "Appearance", type = EditType.DIVIDE))
    private String appearanceSection;

    @EruptField(edit = @Edit(title = "Primary Color", desc = "Applies until a user picks a color of their own, unless app.js locks the appearance",
            type = EditType.COLOR))
    private String primaryColor;

    @EruptField(edit = @Edit(title = "Login Layout", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "center", label = "Centered card"),
            @VL(value = "cover", label = "Form docked right"),
            @VL(value = "wide", label = "Wide card"),
            @VL(value = "wallpaper", label = "Wallpaper"),
            @VL(value = "poster", label = "Poster")
    })))
    private String loginLayout;

    @EruptField(edit = @Edit(title = "Login Background", desc = "Replaces the stock login artwork", type = EditType.ATTACHMENT,
            attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String loginBackground;

}
