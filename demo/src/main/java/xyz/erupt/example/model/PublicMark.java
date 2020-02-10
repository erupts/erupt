package xyz.erupt.example.model;

import lombok.Getter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-09-18.
 */
@Entity
@Table(name = "t_public_mark")
@Erupt(name = "标志信息")
@Getter
public class PublicMark extends BaseModel {

    @EruptField
    private String rawId;

    @EruptField(
            views = @View(title = "标志", type = ViewType.IMAGE),
            edit = @Edit(title = "标志", type = EditType.ATTACHMENT, attachmentType = @AttachmentType(type = AttachmentType.Type.IMAGE))
    )
    private String img;

    @EruptField(
            views = @View(title = "矢量图", type = ViewType.DOWNLOAD),
            edit = @Edit(title = "矢量图", type = EditType.ATTACHMENT)
    )
    private String vector;

    @EruptField(
            views = @View(title = "中文含义"),
            edit = @Edit(title = "中文含义", search = @Search(value = true, vague = true))
    )
    private String mean;

    @EruptField(
            views = @View(title = "英文含义"),
            edit = @Edit(title = "英文含义", search = @Search(value = true, vague = true))
    )
    private String meanEn;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", search = @Search(value = true, vague = true))
    )
    public Date createTime;

    @EruptField(
            views = @View(title = "标准"),
            edit = @Edit(title = "标准")
    )
    private String gb;

    @EruptField(
            views = @View(title = "替换标准"),
            edit = @Edit(title = "替换标准")
    )
    private String replaceGb;

    @EruptField(
            views = @View(title = "解释"),
            edit = @Edit(title = "解释", type = EditType.TEXTAREA)
    )
    private String explainDesc;

    @EruptField(
            views = @View(title = "使用场景"),
            edit = @Edit(title = "使用场景", type = EditType.TEXTAREA, desc = "不同场景之间使用'、'符号隔开")
    )
    private String useScene;

}
