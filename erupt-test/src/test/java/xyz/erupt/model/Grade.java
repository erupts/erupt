package xyz.erupt.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.jpa.model.BaseModel;

@Getter
@Setter
@Entity
public class Grade extends BaseModel {

    private String name;

}
