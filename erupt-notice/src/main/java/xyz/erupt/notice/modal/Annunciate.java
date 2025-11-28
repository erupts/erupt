package xyz.erupt.notice.modal;

import jakarta.persistence.*;
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
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.notice.constant.AnnunciateStatus;
import xyz.erupt.upms.helper.HyperModelCreatorOnlyVo;

@EruptI18n
@Erupt(
        orderBy = "createTime desc",
        name = "Annunciate",
        power = @Power(export = true)
)
@Entity
@Table(name = "e_annunciate")
@Getter
@Setter
public class Annunciate extends HyperModelCreatorOnlyVo {

    @EruptField(
            views = @View(title = "title"),
            edit = @Edit(title = "title", notNull = true, search = @Search(vague = true))
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @EruptField(
            views = @View(title = "status"),
            edit = @Edit(title = "status", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(fetchHandler = AnnunciateStatus.H.class))
    )
    private AnnunciateStatus status = AnnunciateStatus.OPEN;

//    @ManyToOne
//    @NotFound(action = NotFoundAction.IGNORE)
//    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    private EruptPost eruptPost;
//
//    @ManyToOne
//    @NotFound(action = NotFoundAction.IGNORE)
//    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    private EruptOrg eruptOrg;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "content", type = ViewType.HTML),
            edit = @Edit(title = "content", type = EditType.HTML_EDITOR, notNull = true)
    )
    private String content;

}
