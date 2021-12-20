package xyz.erupt.jpa.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2021/3/15 10:23
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(MetaDataProxy.class)
public class MetaModelUpdateVo extends BaseModel {

    private String createBy;

    private LocalDateTime createTime;

    @EruptField(
            views = @View(title = "更新人", width = "100px"),
            edit = @Edit(title = "更新人", readonly = @Readonly)
    )
    private String updateBy;

    @EruptField(
            views = @View(title = "更新时间", sortable = true),
            edit = @Edit(title = "更新时间", readonly = @Readonly, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private LocalDateTime updateTime;

}