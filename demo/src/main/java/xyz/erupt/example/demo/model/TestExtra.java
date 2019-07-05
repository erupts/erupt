package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.DateEnum;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.eruptlimit.model.BaseModel;

import javax.persistence.*;

/**
 * Created by liyuepeng on 2019-05-23.
 */
@Erupt(name = "text Extra")
@Entity
@Table(name = "extra")
public class TestExtra extends BaseModel {
    @Column(name = "IS_MD5")
    @EruptField(
            views = @View(title = "md5加密"),
            edit = @Edit(
                    title = "md5加密",
                    type = EditType.BOOLEAN,
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    ),
                    search = @Search(true)
            )
    )
    private Boolean isMD5;

    @Column(name = "WEEK")
    @EruptField(
            views = @View(title = "周"),
            edit = @Edit(
                    notNull = true,
                    title = "周",
                    search = @Search(value = true, vague = true),
                    type = EditType.DATE,
                    dateType = @DateType(type = DateEnum.MONTH)
            )
    )
    private String week;


    @ManyToOne
    @JoinColumn(name = "EXTRA_LEFT_ID")
    @EruptField(
            views = {
                    @View(title = "姓名", column = "name"),
            }
    )
    private TestExtraLeft testExtraLeft;
}
