package xyz.erupt.example.demo.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by liyuepeng on 2019-08-12.
 */
@Erupt(name = "进销存")
@Entity
@Table(name = "Enters_Sells_Saves")
public class EntersSellsSaves extends BaseModel {

    @EruptField(
            views = @View(title = "品名"),
            edit = @Edit(title = "品名", search = @Search(true))
    )
    @Column(name = "NAME")
    private String name;

    @EruptField(
            views = @View(title = "数量"),
            edit = @Edit(search = @Search(value = true, vague = true), title = "数量", type = EditType.SLIDER)
    )
    @Column(name = "COUNT")
    private Integer count;

    @EruptField(
            views = @View(title = "入库时间"),
            edit = @Edit(search = @Search(value = true, vague = true), title = "入库时间", type = EditType.DATE)
    )
    @Column(name = "DATE")
    private Date date;

    @EruptField(
            views = @View(title = "规格"),
            edit = @Edit(
                    search = @Search(vague = true, value = true),
                    title = "规格",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "XS", label = "XS"),
                            @VL(value = "S", label = "S"),
                            @VL(value = "M", label = "M"),
                            @VL(value = "L", label = "L"),
                            @VL(value = "XL", label = "XL"),
                            @VL(value = "XXL", label = "XXL")
                    })
            )
    )
    @Column(name = "GUIGE")
    private String guige;


    @EruptField(
            views = @View(title = "进价"),
            edit = @Edit(title = "进价")
    )
    @Column(name = "PRICE")
    private Double price;

    @EruptField(
            views = @View(title = "售价"),
            edit = @Edit(title = "售价")
    )
    @Column(name = "SALE_PRICE")
    private Double salePrice;

}
