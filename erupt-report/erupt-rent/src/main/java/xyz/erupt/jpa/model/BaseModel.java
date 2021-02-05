package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.rent.model.EruptRent;

import javax.persistence.*;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(BaseRentDataProxy.class)
public class BaseModel {

    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField
    private Long id;

    @ManyToOne
    private EruptRent eruptRent;

    @Column(length = 12)
    private String rentToken;
}
