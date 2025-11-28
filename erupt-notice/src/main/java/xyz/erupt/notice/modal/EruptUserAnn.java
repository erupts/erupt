package xyz.erupt.notice.modal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2025/11/28 23:41
 */
@Entity
@Table(name = "e_upms_user")
@Getter
@Setter
public class EruptUserAnn extends BaseModel {

    private Long annReadId;

}
