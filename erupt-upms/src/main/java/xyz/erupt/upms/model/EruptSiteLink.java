package xyz.erupt.upms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;

/**
 * One link in the header toolbar of the site settings: help docs, a ticket desk, a support line.
 * Rendered as a plain link next to the app.js r_tools, never as HTML.
 *
 * @author YuePeng
 * date 2026/11/20
 */
@Entity
@Table(name = "e_upms_site_link")
@Erupt(name = "Header Link")
@EruptI18n
@Getter
@Setter
public class EruptSiteLink extends BaseModel {

    @EruptField(views = @View(title = "Link Text"), edit = @Edit(title = "Link Text", notNull = true))
    private String name;

    @EruptField(views = @View(title = "Link URL"), edit = @Edit(title = "Link URL", desc = "http(s)://..., a path on this site, mailto: or tel:", notNull = true))
    private String url;

    @EruptField(views = @View(title = "Icon"), edit = @Edit(title = "Icon", type = EditType.ICON))
    private String icon;

    @EruptField(views = @View(title = "New Window"), edit = @Edit(title = "New Window", type = EditType.BOOLEAN))
    private Boolean newWindow = true;

}
