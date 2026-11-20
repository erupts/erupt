package xyz.erupt.upms.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Layout;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.MultiChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.i18n.EruptLanguage;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.data_proxy.EruptSiteSettingDataProxy;
import xyz.erupt.upms.model.filter.EruptMenuViewFilter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Site settings an administrator edits at runtime: brand (title, logos, favicon, footer), login page (layout, background, a timed notice), appearance defaults, language, navigation
 * (default home, header links) and the watermark. A supplement to {@code app.js}, not a replacement: a value saved here wins over the same
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
@Erupt(name = "Site Settings", layout = @Layout(formSteps = true), dataProxy = EruptSiteSettingDataProxy.class)
@EruptI18n
@Getter
@Setter
public class EruptSiteSetting extends MetaModelUpdateVo {

    @Transient
    @EruptField(edit = @Edit(title = "Brand", type = EditType.DIVIDE))
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

    @EruptField(edit = @Edit(title = "App Icon", desc = "Icon of the installed app (PWA): svg, or a png / jpg / webp of 512px or more", type = EditType.ATTACHMENT,
            attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE)))
    private String pwaIcon;

    @EruptField(edit = @Edit(title = "Footer Text", desc = "Plain text at the foot of the login page, replacing the default copyright line"))
    private String footerText;

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

    @EruptField(edit = @Edit(title = "Registration Page", desc = "Shows a sign-up link on the login page; http(s) or a path on this site"))
    private String registerPage;

    @EruptField(edit = @Edit(title = "Privacy Policy", desc = "Link shown on the login page; http(s) or a path on this site"))
    private String privacyUrl;

    @EruptField(edit = @Edit(title = "Terms of Service", desc = "Link shown on the login page; http(s) or a path on this site"))
    private String termsUrl;

    @EruptField(edit = @Edit(title = "Require Consent", desc = "On: users must tick their agreement to the linked documents before signing in", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = "on", label = "On"),
                    @VL(value = "off", label = "Off")
            })))
    private String requireConsent;

    @Transient
    @EruptField(edit = @Edit(title = "Appearance", type = EditType.DIVIDE))
    private String appearanceSection;

    @EruptField(edit = @Edit(title = "Primary Color", desc = "Applies until a user picks a color of their own, unless app.js locks the appearance",
            type = EditType.COLOR))
    private String primaryColor;

    @EruptField(edit = @Edit(title = "Header Color", desc = "Header bar color, until a user picks one; blank follows the primary color", type = EditType.COLOR))
    private String headerColor;

    // values are the frontend's Skin keys (theme.util.ts)
    @EruptField(edit = @Edit(title = "Skin", desc = "Default until a user picks one; locked appearance forces it", type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                    @VL(value = "default", label = "Default"),
                    @VL(value = "classic", label = "Classic"),
                    @VL(value = "workspace", label = "Workspace"),
                    @VL(value = "material", label = "Material"),
                    @VL(value = "polaris", label = "Polaris"),
                    @VL(value = "linear", label = "Linear"),
                    @VL(value = "liquid-glass", label = "Liquid Glass"),
                    @VL(value = "brutalist", label = "Brutalist"),
                    @VL(value = "terminal", label = "Terminal")
            })))
    private String skin;

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

    @EruptField(edit = @Edit(title = "Compact Mode", desc = "Denser spacing by default, until a user picks", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = "on", label = "On"),
                    @VL(value = "off", label = "Off")
            })))
    private String compact;

    @EruptField(edit = @Edit(title = "Menu Group Titles", desc = "Section titles in the sidebar by module group, until a user picks", type = EditType.CHOICE,
            choiceType = @ChoiceType(vl = {
                    @VL(value = "show", label = "Show"),
                    @VL(value = "hide", label = "Hide")
            })))
    private String menuGroups;

    @EruptField(edit = @Edit(title = "Multiple Tabs", desc = "Open pages as tabs by default, until a user picks", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = "on", label = "On"),
                    @VL(value = "off", label = "Off")
            })))
    private String tabReuse;

    @Transient
    @EruptField(edit = @Edit(title = "Language", type = EditType.DIVIDE))
    private String languageSection;

    @EruptField(edit = @Edit(title = "Default Language", desc = "For users who have not picked one; must be among the offered languages",
            type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = EruptLanguage.Choice.class)))
    private String defaultLang;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(edit = @Edit(title = "Offered Languages", desc = "Languages users can switch to; one hides the language switch",
            type = EditType.MULTI_CHOICE, multiChoiceType = @MultiChoiceType(type = MultiChoiceType.Type.SELECT, fetchHandler = EruptLanguage.Choice.class)))
    private Set<String> locales;

    @Transient
    @EruptField(edit = @Edit(title = "Navigation", type = EditType.DIVIDE))
    private String navigationSection;

    @ManyToOne
    @EruptField(edit = @Edit(title = "Default Home", desc = "Opened after login for users without a home menu of their own, when they may open it",
            type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id"),
            filter = @Filter(conditionHandler = EruptMenuViewFilter.class)))
    private EruptMenu homeMenu;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "site_setting_id", foreignKey = @ForeignKey(name = "fk_upms_site_link_setting"))
    @OrderBy("id")
    @EruptField(edit = @Edit(title = "Header Links", desc = "Plain links in the header toolbar, next to those app.js adds", type = EditType.MULTI_FORM))
    private List<EruptSiteLink> links = new ArrayList<>();

    @Transient
    @EruptField(edit = @Edit(title = "Maintenance", type = EditType.DIVIDE))
    private String maintenanceSection;

    @EruptField(edit = @Edit(title = "Maintenance Mode", desc = "On: only administrators can sign in or keep working; everyone else is signed out at their next request and sees the message", type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = "on", label = "On"),
                    @VL(value = "off", label = "Off")
            })))
    private String maintenance;

    @EruptField(edit = @Edit(title = "Maintenance Message", desc = "Shown on the login page and to refused users; blank uses a standard text", type = EditType.TEXTAREA))
    private String maintenanceMessage;

    @Transient
    @EruptField(edit = @Edit(title = "Watermark", type = EditType.DIVIDE))
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
