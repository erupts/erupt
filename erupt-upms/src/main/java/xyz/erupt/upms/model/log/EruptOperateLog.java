package xyz.erupt.upms.model.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.cube.Dimension;
import xyz.erupt.annotation.cube.EruptCube;
import xyz.erupt.annotation.cube.Measure;
import xyz.erupt.annotation.cube.SqlType;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-05-26
 */
@Entity
@Table(name = "e_upms_operate_log")
@EruptI18n
@Erupt(
        name = "Operation Log",
        desc = "Records all user operations in the platform",
        power = @Power(add = false, edit = false, viewDetails = false,
                delete = false, powerHandler = SuperAdminPower.class, export = true),
        orderBy = "createTime desc",
        dataProxy = EruptOperateLog.class
)
@EruptCube(
        name = "Erupt Operate Log",
        sql = "e_upms_operate_log",
        sqlType = SqlType.TABLE_NAME
)
@Getter
@Setter
public class EruptOperateLog extends BaseModel implements DataProxy<EruptOperateLog> {

    @Dimension(title = "Operate User", sql = "operate_user")
    @EruptField(
            views = @View(title = "Operator"),
            edit = @Edit(title = "Operator", search = @Search)
    )
    private String operateUser;

    @Dimension(title = "IP")
    @EruptField(
            views = @View(title = "IP Address"),
            edit = @Edit(title = "IP Address", search = @Search)
    )
    private String ip;

    @Dimension(title = "IP Region")
    @EruptField(
            views = @View(title = "IP Source", desc = "Country | Region | Province | City | ISP", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP Source", search = @Search)
    )
    private String region;

    @Dimension(title = "API Name", sql = "api_name")
    @EruptField(
            views = @View(title = "Function Name"),
            edit = @Edit(title = "Function Name", search = @Search)
    )
    private String apiName;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Before Data", type = ViewType.CODE),
            edit = @Edit(title = "Before Data", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String beforeData;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Req Param", type = ViewType.CODE),
            edit = @Edit(title = "Req Param", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String reqParam;

    @Dimension(title = "Status")
    @EruptField(
            views = @View(title = "Is Success", sortable = true),
            edit = @Edit(title = "Is Success", search = @Search)
    )
    private Boolean status;

    @Column(length = 4000)
    @EruptField(
            views = @View(title = "Error Info", type = ViewType.HTML)
    )
    private String errorInfo;

    @Dimension(title = "Total Time", sql = "total_time")
    @EruptField(
            views = @View(title = "Req Time", template = "value && value+'ms'", sortable = true),
            edit = @Edit(title = "Req Time", search = @Search)
    )
    private Long totalTime;

    @Dimension(title = "Create Time", sql = "create_time")
    @EruptField(
            views = @View(title = "Record Time", sortable = true),
            edit = @Edit(title = "Record Time", search = @Search, dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date createTime;

    @Dimension(title = "Request Address", sql = "req_addr")
    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "URL", type = ViewType.HTML)
    )
    private String reqAddr;

    @Dimension(title = "Request Method", sql = "req_method")
    @Column(length = 64)
    @EruptField(
            views = @View(title = "Method")
    )
    private String reqMethod;

    @Transient
    @Measure(title = "Count", sql = "count(*)")
    private Long count;

    @Transient
    @Measure(title = "Max Request Duration", sql = "max(total_time)")
    private Long maxRequestDuration;


    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        for (Map<String, Object> map : list) {
            prettyPrintField(gson, map, LambdaSee.field(EruptOperateLog::getBeforeData));
            prettyPrintField(gson, map, LambdaSee.field(EruptOperateLog::getReqParam));
        }
    }

    private void prettyPrintField(Gson gson, Map<String, Object> map, String field) {
        Object val = map.get(field);
        if (null != val) {
            try {
                map.put(field, gson.toJson(gson.fromJson(val.toString(), Object.class)));
            } catch (Exception ignore) {
            }
        }
    }
}
