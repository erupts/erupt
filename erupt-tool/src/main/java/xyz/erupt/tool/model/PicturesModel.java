package xyz.erupt.tool.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.base.BaseModel;

import javax.persistence.Transient;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-05-28
 */
//@Entity
//@Table(name = "E_TOOL_PICTURE")
@Erupt(
        name = "图床管理",
        dataProxy = PicturesModel.class
)
@Getter
@Setter
public class PicturesModel extends BaseModel implements DataProxy {

    @EruptField(
            views = @View(title = "图片地址"),
            edit = @Edit(title = "图片地址")
    )
    private String filePath;

    @Transient
    @EruptField(
            views = @View(title = "缩略图"),
            edit = @Edit(title = "图片地址")
    )
    private String thumbnail;

    @EruptField(
            views = @View(title = "大小"),
            edit = @Edit(title = "大小", show = false, search = @Search(vague = true))
    )
    private String size;

    @EruptField(
            views = @View(title = "宽度"),
            edit = @Edit(title = "宽度", show = false, search = @Search(vague = true))
    )
    private String width;

    @EruptField(
            views = @View(title = "高度"),
            edit = @Edit(title = "高度", show = false, search = @Search(vague = true))
    )
    private String height;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", show = false, search = @Search(vague = true))
    )
    private Date createTime;


}
