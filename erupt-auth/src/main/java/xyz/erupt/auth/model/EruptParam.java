package xyz.erupt.auth.model;

import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.Column;

/**
 * Created by liyuepeng on 2019-11-27.
 */
//@Erupt(
//        name = "平台参数设置"
//)
public class EruptParam {
    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "值"),
            edit = @Edit(title = "值", notNull = true)
    )
    private String value;

    @EruptField(
            edit = @Edit(
                    title = "备注"
            )
    )
    @Column(name = "REMARK")
    private String remark;
}
