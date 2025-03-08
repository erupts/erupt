package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.sub_field.View;

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

    @EruptField(views = @View(title = "创建人", show = false))
    @EruptSmartSkipSerialize
    private String createBy;

    @EruptField(views = @View(title = "创建时间", show = false))
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @EruptField(views = @View(title = "更新人", show = false))
    @EruptSmartSkipSerialize
    private String updateBy;

    @EruptField(views = @View(title = "更新时间", show = false))
    @EruptSmartSkipSerialize
    private LocalDateTime updateTime;

}
