package xyz.erupt.test.model.edit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "AttachmentEdit")
public class AttachmentModel extends BaseModel {

    // 单文件上传（默认）
    @EruptField(
            views = @View(title = "Resume"),
            edit = @Edit(title = "Resume", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            fileTypes = {"pdf", "doc", "docx"},
                            size = 5120))  // 5 MB
    )
    private String resume;

    // 多文件上传
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Attachments"),
            edit = @Edit(title = "Attachments", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            maxLimit = 5,
                            fileSeparator = "|"))
    )
    private String attachments;

    // 图片上传（IMAGE 类型）
    @EruptField(
            views = @View(title = "Avatar"),
            edit = @Edit(title = "Avatar", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.IMAGE,
                            fileTypes = {"jpg", "jpeg", "png", "webp"},
                            size = 2048))  // 2 MB
    )
    private String avatar;

    // 图片尺寸约束
    @EruptField(
            views = @View(title = "Banner"),
            edit = @Edit(title = "Banner", desc = "1920×400 recommended",
                    type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.IMAGE,
                            imageType = @AttachmentType.ImageType(
                                    minWidth = 1280, maxWidth = 3840,
                                    minHeight = 200, maxHeight = 800)))
    )
    private String banner;

    // 多图上传
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Gallery"),
            edit = @Edit(title = "Gallery", type = EditType.ATTACHMENT,
                    attachmentType = @AttachmentType(
                            type = AttachmentType.Type.IMAGE,
                            maxLimit = 9,
                            fileSeparator = "|"))
    )
    private String gallery;
}
