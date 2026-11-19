package xyz.erupt.cloud.example.blog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.RateType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.SliderType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.annotation.sub_field.sub_edit.TextareaType;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The field types of the SDK showcase (erupt-sdk SHOWCASE.md), as a plain erupt entity.
 */
@Entity
@Table(name = "blog_article")
@Erupt(name = "Article", orderBy = "Article.id desc", power = @Power(importable = true), rowOperation = {
        @RowOperation(code = "publish", title = "Publish", icon = "fa fa-paper-plane", ifExpr = "item.status !== 'published'",
                eruptClass = PublishForm.class, operationHandler = ArticleOperations.Publish.class),
        @RowOperation(code = "feature", title = "Feature", mode = RowOperation.Mode.SINGLE, icon = "fa fa-star",
                callHint = "Feature this article on the home page?", operationHandler = ArticleOperations.Feature.class)
})
public class Article extends BaseModel {

    @EruptField(views = @View(title = "Title", sortable = true),
            edit = @Edit(title = "Title", notNull = true, search = @Search, inputType = @InputType(length = 60)))
    private String title;

    @Column(length = 200)
    @EruptField(edit = @Edit(title = "Summary", type = EditType.TEXTAREA, textareaType = @TextareaType(length = 200)))
    private String summary;

    @ManyToOne
    @EruptField(views = @View(title = "Category", column = "name"), edit = @Edit(title = "Category", search = @Search,
            // a table picker, though Category is a tree (which would make the picker a tree)
            type = EditType.REFERENCE_TABLE))
    private Category category;

    @ManyToOne
    @EruptField(edit = @Edit(title = "Topic", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parent.id")))
    private Category topic;

    @EruptField(views = @View(title = "Status"), edit = @Edit(title = "Status", type = EditType.CHOICE, search = @Search,
            choiceType = @ChoiceType(vl = {@VL(value = "draft", label = "Draft"), @VL(value = "review", label = "In Review"),
                    @VL(value = "published", label = "Published")})))
    private String status;

    @EruptField(views = @View(title = "Tags"),
            edit = @Edit(title = "Tags", type = EditType.TAGS, tagsType = @TagsType(allowExtension = true, joinSeparator = ",")))
    private String tags;

    @EruptField(views = @View(title = "Views", sortable = true), edit = @Edit(title = "Views", numberType = @NumberType(min = 0)))
    private Integer views;

    @Column(precision = 12, scale = 2)
    @EruptField(views = @View(title = "Price"), edit = @Edit(title = "Price", numberType = @NumberType(min = 0, precision = 2, prefix = "$")))
    private BigDecimal price;

    @EruptField(views = @View(title = "Rating"),
            edit = @Edit(title = "Rating", type = EditType.RATE, rateType = @RateType(count = 5, allowHalf = true)))
    private Double rating;

    @EruptField(views = @View(title = "Progress"),
            edit = @Edit(title = "Progress", type = EditType.SLIDER, sliderType = @SliderType(min = 0, max = 100, step = 5)))
    private Integer progress;

    @EruptField(views = @View(title = "Featured"), edit = @Edit(title = "Featured", search = @Search))
    private Boolean featured;

    @EruptField(views = @View(title = "Theme Color"), edit = @Edit(title = "Theme Color", type = EditType.COLOR))
    private String color;

    @EruptField(edit = @Edit(title = "Icon", type = EditType.ICON))
    private String icon;

    @EruptField(views = @View(title = "Publish Date"), edit = @Edit(title = "Publish Date", search = @Search))
    private LocalDate publishDate;

    @EruptField(views = @View(title = "Review At", sortable = true), edit = @Edit(title = "Review At"))
    private LocalDateTime reviewAt;

    @Lob
    @EruptField(edit = @Edit(title = "Content", type = EditType.HTML_EDITOR))
    private String content;

    @Lob
    @EruptField(edit = @Edit(title = "Notes", type = EditType.MARKDOWN))
    private String notes;

    @Lob
    @EruptField(edit = @Edit(title = "Meta (JSON)", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json")))
    private String meta;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

}
