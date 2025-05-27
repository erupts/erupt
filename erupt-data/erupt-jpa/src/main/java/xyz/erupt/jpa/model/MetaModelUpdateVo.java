package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.EruptSmartSkipSerialize;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.core.context.MetaContext;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@EruptI18n
@PreDataProxy(MetaModelUpdateVo.Proxy.class)
public class MetaModelUpdateVo extends BaseModel {

    @EruptField(views = @View(title = "创建人", show = false))
    @EruptSmartSkipSerialize
    private String createBy;

    @EruptField(views = @View(title = "创建时间", show = false))
    @EruptSmartSkipSerialize
    private LocalDateTime createTime;

    @EruptField(
            views = @View(title = "更新人", width = "100px"),
            edit = @Edit(title = "更新人", readonly = @Readonly)
    )
    private String updateBy;

    @EruptField(
            views = @View(title = "更新时间"),
            edit = @Edit(title = "更新时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime updateTime;

    public static class Proxy implements DataProxy<MetaModelUpdateVo> {

        @Override
        public void beforeAdd(MetaModelUpdateVo metaModel) {
            metaModel.setCreateTime(LocalDateTime.now());
            metaModel.setCreateBy(MetaContext.getUser().getName());
            metaModel.setUpdateTime(metaModel.getCreateTime());
            metaModel.setUpdateBy(metaModel.getCreateBy());
        }

        @Override
        public void beforeUpdate(MetaModelUpdateVo metaModel) {
            metaModel.setUpdateTime(LocalDateTime.now());
            metaModel.setUpdateBy(MetaContext.getUser().getName());
        }

    }

}