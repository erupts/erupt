package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.context.MetaContext;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(MetaModelCreateOnly.Proxy.class)
public class MetaModelCreateOnly extends BaseModel {

    @Comment("创建人")
    @EruptSmartSkipSerialize
    private String createBy;

    @Comment("创建时间")
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    public static class Proxy implements DataProxy<MetaModelCreateOnly> {

        @Override
        public void beforeAdd(MetaModelCreateOnly metaModel) {
            metaModel.setCreateTime(LocalDateTime.now());
            metaModel.setCreateBy(MetaContext.getUser().getName());
        }

    }


}
