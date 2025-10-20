package xyz.erupt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
public class Student extends BaseModel {

    private String name;

    @ManyToOne
    private Grade grade;

    private Integer age;

}
