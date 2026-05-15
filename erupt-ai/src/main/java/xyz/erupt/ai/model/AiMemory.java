package xyz.erupt.ai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

/**
 * Cross-session AI memory persisted to DB
 *
 * @author YuePeng
 * date 2026/5/15
 */
@Getter
@Setter
@Table(name = "e_ai_memory")
@Entity
@EruptI18n
public class AiMemory extends BaseModel {

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    private String content;

    private Long userId;

    private LocalDateTime createTime;

}
