package xyz.erupt.notice.modal;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.jpa.model.BaseModel;

import java.time.LocalDateTime;

@EruptI18n
@Erupt(
        orderBy = "createdAt desc",
        name = "Notice Log",
        power = @Power(export = true, add = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_notice_log")
@Getter
@Setter
public class NoticeLog extends BaseModel {

    private LocalDateTime createdAt;

}
