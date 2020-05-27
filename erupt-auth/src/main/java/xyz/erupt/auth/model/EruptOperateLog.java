package xyz.erupt.auth.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2020-05-26
 */
@Entity
@Table(name = "E_OPERATE_LOG")
@Erupt(
        name = "操作日志",
        power = @Power(add = false, edit = false, viewDetails = false, delete = false, export = true),
        orderBy = "createTime desc"
)
@Getter
@Setter
public class EruptOperateLog extends BaseModel {

    @ManyToOne
    @EruptField(
            views = @View(title = "用户", column = "name"),
            edit = @Edit(title = "用户", type = EditType.REFERENCE_TABLE, search = @Search)
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "IP"),
            edit = @Edit(title = "IP", search = @Search)
    )
    private String ip;

    @EruptField(
            views = @View(title = "功能名称"),
            edit = @Edit(title = "功能名称", search = @Search)
    )
    private String apiName;

    @EruptField(
            views = @View(title = "是否成功"),
            edit = @Edit(title = "是否成功", search = @Search)
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "请求耗时", template = "value && value+'ms'"),
            edit = @Edit(title = "请求耗时", search = @Search(vague = true))
    )
    private Long totalTime;

    @EruptField(
            views = @View(title = "请求时间"),
            edit = @Edit(title = "请求时间", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date createTime;

    @Lob
    @EruptField(
            views = @View(title = "请求地址")
    )
    private String reqAddr;

    @Lob
//    @EruptField(
//            views = @View(title = "请求方法")
//    )
    private String reqMethod;

    @Lob
//    @EruptField(
//            views = @View(title = "请求参数", type = ViewType.HTML)
//    )
    private String reqParam;

    @Lob
    @EruptField(
            views = @View(title = "错误信息", type = ViewType.HTML)
    )
    private String errorInfo;

}
