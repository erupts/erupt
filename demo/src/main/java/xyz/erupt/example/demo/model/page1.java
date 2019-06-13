package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.eruptlimit.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by liyuepeng on 2019-06-12.
 */
@Erupt(
        name = "事例页面",
        loginUse = false,
        sorts = "id"
)
@Entity
@Table(name = "PAEG1")
public class page1 extends BaseModel {

    @Column(name = "NAME")
    @EruptField(
            views = {
                    @View(title = "项目名称")
            },
            edit = @Edit(
                    title = "项目名称",
                    notNull = true,
                    search = @Search(true)
            )
    )
    private String name;

    @Column(name = "TYPE")
    @EruptField(
            views = {
                    @View(title = "启动类型")
            },
            edit = @Edit(
                    title = "启动类型",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(
                                            value = "1",
                                            label = "立刻"
                                    ),
                                    @VL(
                                            value = "2",
                                            label = "其他"
                                    )
                            }
                    ),
                    search = @Search(true)
            )
    )
    private String type;

    @Column(name = "DATE")
    @EruptField(
            views = {
                    @View(title = "启动扫描时间")
            },
            edit = @Edit(
                    title = "启动扫描时间",
                    notNull = true,
                    type = EditType.DATE,
                    search = @Search(true)
            )
    )
    private Date date;

    @Column(name = "IP")
    @EruptField(
            views = {
                    @View(title = "IP地址")
            },
            edit = @Edit(
                    title = "IP地址",
                    notNull = true,
                    type = EditType.TEXTAREA
            )
    )
    private String ip;
}
