package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.Comment;
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

    @Comment("创建人")
    @EruptSmartSkipSerialize
    private String createBy;

    @Comment("创建时间")
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @Comment("更新人")
    @EruptSmartSkipSerialize
    private String updateBy;

    @Comment("更新时间")
    @EruptSmartSkipSerialize
    private LocalDateTime updateTime;

}
