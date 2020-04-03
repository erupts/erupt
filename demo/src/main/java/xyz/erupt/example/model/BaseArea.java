package xyz.erupt.example.model;


import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.core.model.BaseModel;

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

    @EruptField
    private String name;

    @EruptField
    private Integer level;

    @ManyToOne
    @JoinColumn(name = "pid")
    private BaseArea pid;

}
