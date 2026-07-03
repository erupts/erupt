package xyz.erupt.cloud.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.constant.PageEmbedType;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.cloud.server.base.CloudServerConst;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.handler.ViaMenuValueCtrl;

/**
 * @author YuePeng
 * date 2021/12/16 00:28
 */
@Slf4j
@Getter
@Setter
@Entity
@Table(name = "e_cloud_node")
@Erupt(
        name = "Node Config", dataProxy = CloudNodeProcess.class,
        linkTree = @LinkTree(field = "cloudNodeGroup"),
        rowOperation = {
                @RowOperation(
                        title = "View Token", icon = "fa fa-shield", mode = RowOperation.Mode.SINGLE,
                        show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = CloudServerConst.CLOUD_ACCESS_TOKEN_PERMISSION),
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = "/tpl/node-info.ftl", embedType = PageEmbedType.MICRO_FRONTEND)
                ),
                @RowOperation(
                        title = "Node Log", mode = RowOperation.Mode.SINGLE,
                        ifExpr = "item.version && item.version !== '-'",
                        show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = CloudServerConst.ERUPT_CLOUD_NODE_LOG),
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = "/tpl/erupt-log.html", height = "80vh", openWay = OpenWay.DRAWER, drawerPlacement = Placement.BOTTOM)
                ),
        }, layout = @Layout(tableLeftFixed = 1, pageSize = 30)
)
@Component
@EruptI18n
public class CloudNode extends MetaModelUpdateVo {

    @Column(unique = true)
    @EruptField(
            views = @View(title = "Node Name", sortable = true),
            edit = @Edit(title = "Node Name", desc = "NodeName", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String nodeName;

    @EruptField(
            views = @View(title = "Friendly Name", sortable = true),
            edit = @Edit(title = "Friendly Name", notNull = true, search = @Search(operator = QueryExpression.LIKE))
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "Node Group", column = "name"),
            edit = @Edit(title = "Node Group", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private CloudNodeGroup cloudNodeGroup;

    @EruptField(
            views = @View(title = "Status", sortable = true),
            edit = @Edit(title = "Status", search = @Search, notNull = true, boolType = @BoolType(
                    trueText = "Enable", falseText = "Disable"
            ))
    )
    private Boolean status = true;

    @Transient
    @EruptField(
            views = @View(title = "Erupt Class Count", className = "text-center", width = "120px")
    )
    private Integer eruptNum;

    @Transient
    @EruptField(
            views = @View(title = "Module Count", className = "text-center", width = "70px")
    )
    private Integer eruptModuleNum;

    @Transient
    @EruptField(
            views = @View(title = "Instance Count", className = "text-center", width = "70px"
                    , tpl = @Tpl(path = "/tpl/node-instance.ftl", width = "400px", tplHandler = CloudNodeProcess.class)
            )
    )
    private Integer instanceNum;

    @Transient
    @EruptField(
            views = @View(title = "Version", className = "text-center", width = "120px")
    )
    private String version;

    @EruptField(
            views = @View(title = "Duty", sortable = true),
            edit = @Edit(title = "Duty", type = EditType.TAGS,
                    tagsType = @TagsType(fetchHandler = CloudNodeProcess.class))
    )
    private String duty;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            views = @View(title = "Node Config"),
            edit = @Edit(title = "Node Config", desc = "Config can be read by child nodes", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

    @EruptField(
            views = @View(title = "Access Token", width = "120px")
    )
    private String accessToken;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Description", type = ViewType.HTML),
            edit = @Edit(title = "Description", type = EditType.TEXTAREA)
    )
    private String remark;


}
