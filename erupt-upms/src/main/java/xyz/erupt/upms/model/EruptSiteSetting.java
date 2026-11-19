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
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.data_proxy.EruptSiteSettingDataProxy;

import java.time.LocalDateTime;

/**
 * Site settings an administrator edits at runtime: brand (title, logos, favicon, footer, ICP
 * number), login page (layout, background, a timed notice), appearance defaults and the watermark. A supplement to {@code app.js}, not a replacement: a value saved here wins over the same
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

    @EruptField(edit = @Edit(title = "Footer Text", desc = "Plain text at the foot of the login page, replacing the default copyright line"))
    private String footerText;

    @EruptField(edit = @Edit(title = "ICP Number", desc = "Shown at the foot of the login page, linked to beian.miit.gov.cn; required for sites served in mainland China"))
    private String icpNumber;

    @EruptField(edit = @Edit(title = "Copyright Line", desc = "Blank follows app.js", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "show", label = "Show"),
            @VL(value = "hide", label = "Hide")
    })))
    private String copyright;

    @Transient
    @EruptField(edit = @Edit(title = "Login Page", type = EditType.DIVIDE))
    private String loginSection;

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

    @EruptField(edit = @Edit(title = "Login Notice", desc = "A banner above the login form, e.g. a maintenance window; blank shows none",
            type = EditType.TEXTAREA))
    private String loginNotice;

    @EruptField(edit = @Edit(title = "Notice From", desc = "Blank means at once", dateType = @DateType(type = DateType.Type.DATE_TIME)))
    private LocalDateTime loginNoticeStart;

    @EruptField(edit = @Edit(title = "Notice Until", desc = "Blank means until removed", dateType = @DateType(type = DateType.Type.DATE_TIME)))
    private LocalDateTime loginNoticeEnd;

    @Transient
    @EruptField(edit = @Edit(title = "Appearance", type = EditType.DIVIDE))
    private String appearanceSection;

    @EruptField(edit = @Edit(title = "Primary Color", desc = "Applies until a user picks a color of their own, unless app.js locks the appearance",
            type = EditType.COLOR))
    private String primaryColor;

    @EruptField(edit = @Edit(title = "Color Scheme", desc = "Default until a user picks light or dark", type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                    @VL(value = "light", label = "Light"),
                    @VL(value = "dark", label = "Dark"),
                    @VL(value = "auto", label = "Follow the system")
            })))
    private String colorScheme;

    @EruptField(edit = @Edit(title = "Menu Mode", desc = "Default until a user picks one", type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                    @VL(value = "normal", label = "Sidebar"),
                    @VL(value = "split", label = "Categories in the header"),
                    @VL(value = "dual", label = "Two-column sidebar"),
                    @VL(value = "top", label = "Header only"),
                    @VL(value = "group", label = "Group titles"),
                    @VL(value = "top-split", label = "Header with second row")
            })))
    private String menuMode;

    @EruptField(edit = @Edit(title = "Form Panel", desc = "How record forms open, until a user picks one", type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                    @VL(value = "center", label = "Floating dialog"),
                    @VL(value = "side", label = "Right panel"),
                    @VL(value = "full", label = "Fullscreen")
            })))
    private String formPanelMode;

    @EruptField(edit = @Edit(title = "Lock Appearance", desc = "Locked: users can no longer change colors, skin or menu mode, and their earlier choices are dropped; light/dark and compact stay theirs",
            type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = "allow", label = "Users may customize"),
                    @VL(value = "lock", label = "Locked to these settings")
            })))
    private String appearanceLock;

    @Transient
    @EruptField(edit = @Edit(title = "Watermark", desc = "Blank follows erupt-app.water-mark in the application configuration", type = EditType.DIVIDE))
    private String watermarkSection;

    @EruptField(edit = @Edit(title = "Watermark", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "on", label = "On"),
            @VL(value = "off", label = "Off")
    })))
    private String waterMark;

    @EruptField(edit = @Edit(title = "Watermark Text", desc = "Added after the user's name, e.g. Internal"))
    private String waterMarkContent;

    @EruptField(edit = @Edit(title = "Watermark Date", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
            @VL(value = "on", label = "Show"),
            @VL(value = "off", label = "Hide")
    })))
    private String waterMarkDate;


}
