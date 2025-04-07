package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
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
 * date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(MetaModelCreateOnlyVo.Proxy.class)
public class MetaModelCreateOnlyVo extends MetaModelCreateOnly {

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

    public static class Proxy implements DataProxy<MetaModelCreateOnlyVo> {

        @Override
        public void editBehavior(MetaModelCreateOnlyVo metaModelUpdateVo) {
            metaModelUpdateVo.setCreateByVo(metaModelUpdateVo.getCreateBy());
            metaModelUpdateVo.setCreateTimeVo(metaModelUpdateVo.getCreateTime());
        }

        @Override
        public void afterFetch(Collection<Map<String, Object>> list) {
            for (Map<String, Object> map : list) {
                map.put(LambdaSee.field(MetaModelCreateOnlyVo::getCreateByVo), map.get(LambdaSee.field(MetaModelCreateOnly::getCreateBy)));
                map.put(LambdaSee.field(MetaModelCreateOnlyVo::getCreateTimeVo), map.get(LambdaSee.field(MetaModelCreateOnly::getCreateTime)));
            }
        }
    }

}
