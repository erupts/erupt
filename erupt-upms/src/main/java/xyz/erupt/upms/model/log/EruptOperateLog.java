package xyz.erupt.upms.model.log;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUser;

import javax.persistence.*;
import java.util.Date;

/**
 * @author YuePeng
 * date 2020-05-26
 */
@Entity
@Table(name = "e_upms_operate_log")
@Erupt(
        name = "操作日志",
        power = @Power(add = false, edit = false, viewDetails = false, delete = false),
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
            views = @View(title = "IP来源", desc = "格式：国家 | 大区 | 省份 | 城市 | 运营商", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP来源", search = @Search(vague = true))
    )
    private String region;

    @EruptField(
            views = @View(title = "功能名称"),
            edit = @Edit(title = "功能名称", search = @Search(vague = true))
    )
    private String apiName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "请求参数", type = ViewType.CODE),
            edit = @Edit(title = "请求参数", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String reqParam;

    @EruptField(
            views = @View(title = "是否成功"),
            edit = @Edit(title = "是否成功", search = @Search)
    )
    private Boolean status;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "错误信息", type = ViewType.HTML)
    )
    private String errorInfo;

    @EruptField(
            views = @View(title = "请求耗时", template = "value && value+'ms'"),
            edit = @Edit(title = "请求耗时", search = @Search(vague = true))
    )
    private Integer totalTime;

    @EruptField(
            views = @View(title = "记录时间"),
            edit = @Edit(title = "记录时间", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date createTime;

    @Column(length = 500)
    @EruptField(
            views = @View(title = "请求地址", type = ViewType.HTML)
    )
    private String reqAddr;

    @EruptField(
            views = @View(title = "请求方法")
    )
    private String reqMethod;

}
