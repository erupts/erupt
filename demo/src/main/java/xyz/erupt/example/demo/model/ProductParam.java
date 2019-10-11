package xyz.erupt.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.auth.model.EruptUser;

import javax.persistence.*;

/**
 * Created by liyuepeng on 2019-07-29.
 */
@Erupt(name = "产品参数")
@Entity
@Table(name = "PRODUCT_PARAMS")
@Getter
@Setter
public class ProductParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @EruptField
    private Long id;

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "参数名称", sortable = true),
            edit = @Edit(title = "参数名称", notNull = true, search = @Search(true))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @EruptField(
            views = {
                    @View(title = "姓名", column = "name"),
                    @View(title = "用户名", column = "account")
            },
            edit = @Edit(
                    title = "用户",
                    type = EditType.REFERENCE_TABLE,
                    search = @Search(value = true)
            )
    )
    private EruptUser eruptUser;

    @Column(name = "FLOAT1")
    @EruptField(
            views = @View(title = "参考浮点", sortable = true),
            edit = @Edit(title = "参考浮点", notNull = true)
    )
    private Double float1;


//    @ManyToOne
//    @JoinColumn(name = "PRODUCT_ID")
//    private Product product;

}
