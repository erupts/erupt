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
 * date 2021/7/2 10:23
 */
@Getter
@Setter
@MappedSuperclass
@EruptI18n
@PreDataProxy(MetaModelCreateVo.Proxy.class)
public class MetaModelCreateVo extends MetaModel {

    @Transient
    @EruptField(
            views = @View(title = "创建人", width = "100px"),
            edit = @Edit(title = "创建人", readonly = @Readonly)
    )
    private String createByVo;

    @Transient
    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime createTimeVo;

    public static class Proxy implements DataProxy<MetaModelCreateVo> {

        @Override
        public void editBehavior(MetaModelCreateVo metaModelUpdateVo) {
            metaModelUpdateVo.setCreateByVo(metaModelUpdateVo.getCreateBy());
            metaModelUpdateVo.setCreateTimeVo(metaModelUpdateVo.getCreateTime());
        }

        @Override
        public void afterFetch(Collection<Map<String, Object>> list) {
            for (Map<String, Object> map : list) {
                map.put(LambdaSee.field(MetaModelCreateVo::getCreateByVo), map.get(LambdaSee.field(MetaModel::getCreateBy)));
                map.put(LambdaSee.field(MetaModelCreateVo::getCreateTimeVo), map.get(LambdaSee.field(MetaModel::getCreateTime)));
            }
        }
    }

}