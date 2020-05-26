package xyz.erupt.example.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author liyuepeng
 * @date 2019-08-12.
 */
@Erupt(name = "进销存", power = @Power(importable = true, export = true), filter = @Filter("1=1"))
@Entity
@Table(name = "Enters_Sells_Saves")
public class EntersSellsSaves extends BaseModel {

    @EruptField(
            views = @View(title = "品名"),
            edit = @Edit(title = "品名", search = @Search)
    )
    @Column(name = "NAME")
    private String name;

    @EruptField(
            views = @View(title = "数量"),
            edit = @Edit(search = @Search(vague = true), title = "数量", type = EditType.SLIDER)
    )
    @Column(name = "COUNT")
    private Integer count;

    @EruptField(
            views = @View(title = "入库时间"),
            edit = @Edit(search = @Search(value = true, vague = true), title = "入库时间", readOnly = true)
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
                    }, fetchHandler = Demo.class,
                            fetchHandlerParams = {"param"})
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

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "sell_id")
    @EruptField(
            edit = @Edit(title = "Demos", type = EditType.TAB_TABLE_ADD)
    )
    private Set<Demo> demos;

}
