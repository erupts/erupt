package xyz.erupt.report.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_DATASOURCE")
@Erupt(name = "数据源")
public class BiDataSource extends BaseModel {

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "驱动"),
            edit = @Edit(title = "驱动", notNull = true)
    )
    private String driver;

    @EruptField(
            views = @View(title = "链接字符串"),
            edit = @Edit(title = "链接字符串", type = EditType.TEXTAREA, notNull = true)
    )
    private String url;

    @EruptField(
            views = @View(title = "用户名"),
            edit = @Edit(title = "用户名", notNull = true)
    )
    private String userName;

    @EruptField(
            views = @View(title = "密码"),
            edit = @Edit(title = "密码")
    )
    private String password;

}
