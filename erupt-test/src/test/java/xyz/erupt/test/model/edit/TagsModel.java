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
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
@Erupt(name = "TagsEdit")
public class TagsModel extends BaseModel {

    // 静态候选标签 + 允许自定义
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Skills"),
            edit = @Edit(title = "Skills", type = EditType.TAGS,
                    tagsType = @TagsType(tags = {"Java", "Spring", "Erupt", "Go", "Python"}))
    )
    private String skills;

    // 禁止自定义，只能选预设
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Category"),
            edit = @Edit(title = "Category", type = EditType.TAGS,
                    tagsType = @TagsType(
                            tags = {"Tech", "Finance", "Health", "Education"},
                            allowExtension = false))
    )
    private String category;

    // 最多选 3 个
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Highlights"),
            edit = @Edit(title = "Highlights", desc = "Max 3 tags",
                    type = EditType.TAGS,
                    tagsType = @TagsType(
                            tags = {"Hot", "New", "Recommend", "Sale", "Limited"},
                            maxTagCount = 3))
    )
    private String highlights;

    // 自定义分隔符（逗号）
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Keywords"),
            edit = @Edit(title = "Keywords", type = EditType.TAGS,
                    search = @Search(value = true, vague = true),
                    tagsType = @TagsType(joinSeparator = ","))
    )
    private String keywords;

    // 动态 handler 获取候选标签
    @Column(columnDefinition = "TEXT")
    @EruptField(
            views = @View(title = "Labels"),
            edit = @Edit(title = "Labels", type = EditType.TAGS,
                    tagsType = @TagsType(fetchHandler = {TestTagsFetchHandler.class}))
    )
    private String labels;
}
