package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.linq.lambda.LambdaSee;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@EruptI18n
@PreDataProxy(MetaModelUpdateVo.Proxy.class)
public class MetaModelUpdateVo extends MetaModel {

    @Transient
    @EruptField(
            views = @View(title = "更新人", width = "100px"),
            edit = @Edit(title = "更新人", readonly = @Readonly)
    )
    private String updateByVo;

    @Transient
    @EruptField(
            views = @View(title = "更新时间"),
            edit = @Edit(title = "更新时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime updateTimeVo;

    public static class Proxy implements DataProxy<MetaModelUpdateVo> {

        @Override
        public void editBehavior(MetaModelUpdateVo metaModelUpdateVo) {
            metaModelUpdateVo.setUpdateByVo(metaModelUpdateVo.getUpdateBy());
            metaModelUpdateVo.setUpdateTimeVo(metaModelUpdateVo.getUpdateTime());
        }

        @Override
        public void afterFetch(Collection<Map<String, Object>> list) {
            for (Map<String, Object> map : list) {
                map.put(LambdaSee.field(MetaModelUpdateVo::getUpdateByVo), map.get(LambdaSee.field(MetaModel::getUpdateBy)));
                map.put(LambdaSee.field(MetaModelUpdateVo::getUpdateTimeVo), map.get(LambdaSee.field(MetaModel::getUpdateTime)));
            }
        }
    }

}