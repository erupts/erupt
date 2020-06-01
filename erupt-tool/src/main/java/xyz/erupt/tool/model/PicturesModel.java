package xyz.erupt.tool.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.Transient;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-05-28
 */
//@Entity
//@Table(name = "E_TOOL_PICTURE")
@Erupt(
        name = "图床管理"
)
@Getter
@Setter
public class PicturesModel {

    @EruptField(
            views = @View(title = "图片地址"),
            edit = @Edit(title = "图片地址")
    )
    private String filePath;

    @Transient
    @EruptField(
            views = @View(title = "缩略图"),
            edit = @Edit(title = "缩略图")
    )
    private String thumbnail;

    @EruptField(
            views = @View(title = "大小"),
            edit = @Edit(title = "大小")
    )
    private String size;

    @EruptField(
            views = @View(title = "宽度"),
            edit = @Edit(title = "宽度")
    )
    private String width;

    @EruptField(
            views = @View(title = "高度"),
            edit = @Edit(title = "高度")
    )
    private String height;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间")
    )
    private Date createTime;


}
