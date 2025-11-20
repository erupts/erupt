package xyz.erupt.notice.modal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

@EruptI18n
@Erupt(name = "Notice Scene")
@Entity
@Table(name = "e_notice_scene")
@Getter
@Setter
public class NoticeScene extends HyperModelUpdateVo {

    @Column(length = AnnotationConst.CODE_LENGTH, unique = true)
    @EruptField(
            views = @View(title = "code", sortable = true),
            edit = @Edit(title = "code", notNull = true, search = @Search(vague = true))
    )
    private String code;

    @EruptField(
            views = @View(title = "name", sortable = true),
            edit = @Edit(title = "name", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "remark"),
            edit = @Edit(title = "remark", type = EditType.TEXTAREA)
    )
    private String remark;

}
