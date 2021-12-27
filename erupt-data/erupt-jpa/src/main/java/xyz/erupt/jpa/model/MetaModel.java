package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(MetaDataProxy.class)
public class MetaModel extends BaseModel {

    @EruptSmartSkipSerialize
    private String createBy;

    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @EruptSmartSkipSerialize
    private String updateBy;

    @EruptSmartSkipSerialize
    private LocalDateTime updateTime;

}
