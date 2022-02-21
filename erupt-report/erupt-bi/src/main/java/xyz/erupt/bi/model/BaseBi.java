package xyz.erupt.bi.model;

import xyz.erupt.jpa.model.MetaModelUpdateVo;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2022/2/22 00:20
 */
@Entity
@Table(name = "e_bi")
public class BaseBi extends MetaModelUpdateVo {

    public BaseBi(Long id) {
        this.setId(id);
    }

    public BaseBi() {
    }
}