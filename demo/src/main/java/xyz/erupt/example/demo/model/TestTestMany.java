package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.auth.model.BaseModel;

import javax.persistence.*;

/**
 * Created by liyuepeng on 2019-07-18.
 */
@Erupt(
        name = "测试"
)
@Entity
@Table(name = "TEST_AND_TEST_MANY")
public class TestTestMany extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "TEST_ID")
    private Test test;

    @ManyToOne
    @JoinColumn(name = "TEST_MANY_ID")
    @EruptField(
            views = @View(title = "MANY", column = "name"),
            edit = @Edit(
                    title = "MANY",
                    type = EditType.REFERENCE_TREE
            )
    )
    private TestMany testMany;

    @Column(name = "PRICE")
    @EruptField(
            views = @View(title = "价格", column = "name"),
            edit = @Edit(
                    title = "价格"
            )
    )
    private Double price;


}
