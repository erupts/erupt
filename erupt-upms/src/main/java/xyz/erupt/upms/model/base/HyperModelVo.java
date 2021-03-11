package xyz.erupt.upms.model.base;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.config.SkipSerialize;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2018-10-11.
 */
@Getter
@Setter
@MappedSuperclass
@PreDataProxy(HyperDataProxyVo.class)
public class HyperModelVo extends BaseModel {

    @Transient
    @EruptField(
            edit = @Edit(title = "数据审计", type = EditType.DIVIDE)
    )
    private String divide;

    @EruptField(
            views = @View(title = "创建时间"),
            edit = @Edit(title = "创建时间", readOnly = true)
    )
    private Date createTime;

    @ManyToOne
    @EruptField(
            views = @View(title = "创建人", column = "name"),
            edit = @Edit(title = "创建人", readOnly = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser createUser;

    @EruptField(
            views = @View(title = "更新时间"),
            edit = @Edit(title = "更新时间", readOnly = true)
    )
    private Date updateTime;

    @ManyToOne
    @EruptField(
            views = @View(title = "更新人", column = "name"),
            edit = @Edit(title = "更新人", readOnly = true, type = EditType.REFERENCE_TABLE)
    )
    private EruptUser updateUser;

}
