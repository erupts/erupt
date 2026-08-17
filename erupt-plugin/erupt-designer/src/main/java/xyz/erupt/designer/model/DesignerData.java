package xyz.erupt.designer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

/**
 * Row storage for designer models: one JSON row per record, schema-free on any RDBMS.
 *
 * @author YuePeng
 * date 2026-06-12
 */
@Entity
@Table(name = "e_designer_data", indexes = @Index(name = "idx_designer_data_model", columnList = "model"))
@Getter
@Setter
public class DesignerData extends BaseModel {

    @Column(length = 64, nullable = false)
    private String model;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String data;

}
