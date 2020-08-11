package xyz.demo.erupt.example.model;


import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.auth.model.base.BaseModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "base_area")
@Erupt(
        name = "地区"
)
public class BaseArea extends BaseModel {

    @EruptField(views = @View(title = "名称"))
    private String name;

    @EruptField(views = @View(title = "层级"))
    private Integer level;

    @ManyToOne
    @JoinColumn(name = "pid")
    private BaseArea pid;

}
