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
import java.util.Set;

/**
 * Created by liyuepeng on 2019-07-29.
 */
@Erupt(
        name = "产品"
)
@Entity
@Table(name = "PRODUCT")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @EruptField
    private Long id;


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

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "产品名称", sortable = true),
            edit = @Edit(title = "产品名称", notNull = true, search = @Search(true))
    )
    private String name = "xxx";


    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL})
    @EruptField(
            edit = @Edit(
                    title = "参数列表",
                    type = EditType.TAB_TABLE_ADD
            )
    )
    private Set<ProductParam> productParam;
}
