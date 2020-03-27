package xyz.erupt.example.model;

import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by liyuepeng on 2019-09-18.
 */
@Erupt(name = "新闻发布", dataProxy = News.class)
@Entity
@Table(name = "t_xinwen")
@Setter
public class News extends BaseModel implements DataProxy<News> {

    @EruptField(
            views = @View(title = "标题"),
            edit = @Edit(title = "标题", search = @Search(value = true, vague = true), notNull = true)
    )
    private String title;

    @EruptField(
            views = @View(title = "发布时间")
    )
    private Date createTime;

    @Lob
    @EruptField(
            views = @View(title = "简介"),
            edit = @Edit(title = "简介", type = EditType.TEXTAREA, notNull = true)
    )
    private String hint;

    @Lob
    @EruptField(
            views = @View(title = "内容", type = ViewType.HTML),
            edit = @Edit(title = "内容", type = EditType.HTML_EDIT, notNull = true)
    )
    private String content;

    @Override
    public void beforeAdd(News o) {
        o.setCreateTime(new Date());
    }
}
