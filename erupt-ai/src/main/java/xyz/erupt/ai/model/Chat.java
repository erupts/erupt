package xyz.erupt.ai.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.jpa.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2025/2/27 22:57
 */
@Getter
@Setter
@Table(name = "e_ai_chat")
@Erupt(name = "会话管理")
@Entity
@EruptI18n
public class Chat extends BaseModel {

    private String title;

    private Long userId;


}
