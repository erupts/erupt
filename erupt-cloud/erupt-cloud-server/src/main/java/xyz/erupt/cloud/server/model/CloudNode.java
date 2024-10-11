package xyz.erupt.cloud.server.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
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

import javax.persistence.*;

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
        name = "节点配置", dataProxy = CloudNodeProcess.class,
        rowOperation = {
                @RowOperation(
                        title = "查看令牌", icon = "fa fa-shield", mode = RowOperation.Mode.SINGLE,
                        show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = CloudServerConst.CLOUD_ACCESS_TOKEN_PERMISSION),
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = "/tpl/node-info.ftl")
                ),
                @RowOperation(
                        title = "节点日志", mode = RowOperation.Mode.SINGLE,
                        ifExpr = "item.version && item.version != '-'",
                        show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = CloudServerConst.ERUPT_CLOUD_NODE_LOG),
                        type = RowOperation.Type.TPL, tpl = @Tpl(path = "/tpl/erupt-log.html", height = "80%", openWay = OpenWay.DRAWER, drawerPlacement = Placement.BOTTOM)
                ),
        }, layout = @Layout(tableLeftFixed = 1, pageSize = 30)
)
@Component
@EruptI18n
public class CloudNode extends MetaModelUpdateVo {

    @Column(unique = true)
    @EruptField(
            views = @View(title = "节点名", sortable = true),
            edit = @Edit(title = "节点名", desc = "NodeName", notNull = true, search = @Search(vague = true))
    )
    private String nodeName;

    @EruptField(
            views = @View(title = "友好名称", sortable = true),
            edit = @Edit(title = "友好名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @EruptField(
            views = @View(title = "所属分组", column = "name"),
            edit = @Edit(title = "所属分组", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private CloudNodeGroup cloudNodeGroup;

    @EruptField(
            views = @View(title = "状态", sortable = true),
            edit = @Edit(title = "状态", search = @Search, notNull = true, boolType = @BoolType(
                    trueText = "启用", falseText = "禁用"
            ))
    )
    private Boolean status = true;

    @Transient
    @EruptField(
            views = @View(title = "Erupt 类数量", className = "text-center", width = "120px")
    )
    private Integer eruptNum;

    @Transient
    @EruptField(
            views = @View(title = "模块数", className = "text-center", width = "70px")
    )
    private Integer eruptModuleNum;

    @Transient
    @EruptField(
            views = @View(title = "实例数", className = "text-center", width = "70px"
                    , tpl = @Tpl(path = "/tpl/node-instance.ftl", width = "400px", tplHandler = CloudNodeProcess.class)
            )
    )
    private Integer instanceNum;

    @Transient
    @EruptField(
            views = @View(title = "版本", className = "text-center", width = "120px")
    )
    private String version;

    @EruptField(
            views = @View(title = "负责人", sortable = true),
            edit = @Edit(title = "负责人", type = EditType.TAGS,
                    tagsType = @TagsType(fetchHandler = CloudNodeProcess.class), notNull = true)
    )
    private String duty;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "节点配置"),
            edit = @Edit(title = "节点配置", desc = "配置后可在子节点中读取", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

    @EruptField(
            views = @View(title = "Access Token", width = "120px")
    )
    private String accessToken;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "描述", type = ViewType.HTML),
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;


}
