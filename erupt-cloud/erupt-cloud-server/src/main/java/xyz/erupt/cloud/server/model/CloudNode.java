package xyz.erupt.cloud.server.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.cloud.server.base.CloudServerConst;
import xyz.erupt.cloud.server.node.MetaNode;
import xyz.erupt.cloud.server.node.NodeManager;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.handler.ViaMenuValueCtrl;

import javax.annotation.Resource;
import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/12/16 00:28
 */
@Getter
@Setter
@Entity
@Table(name = "e_cloud_node", uniqueConstraints = @UniqueConstraint(columnNames = CloudNode.NODE_NAME))
@Erupt(name = "节点配置", dataProxy = CloudNode.class,
        rowOperation = @RowOperation(
                title = "查看令牌", icon = "fa fa-shield", mode = RowOperation.Mode.SINGLE,
                show = @ExprBool(exprHandler = ViaMenuValueCtrl.class, params = CloudServerConst.CLOUD_ACCESS_TOKEN_PERMISSION),
                type = RowOperation.Type.TPL, tpl = @Tpl(path = "/tpl/NodeInfo.ftl")
        )
)
@Component
public class CloudNode extends MetaModelUpdateVo implements DataProxy<CloudNode>, TagsFetchHandler {

    public static final String NODE_NAME = "nodeName";

    private static final String REGISTER_TYPE = "registerType";

    private static final String ADDRESSES = "addresses";

    public static final String ACCESS_TOKEN = "accessToken";

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
            views = @View(title = "实例数", className = "text-center", width = "60px")
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
                    tagsType = @TagsType(fetchHandler = CloudNode.class), notNull = true)
    )
    private String duty;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "节点配置"),
            edit = @Edit(title = "节点配置", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String config;

    @EruptField(
            views = @View(title = "Access Token", width = "120px")
    )
    private String accessToken;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = @View(title = "描述", type = ViewType.HTML),
            edit = @Edit(title = "描述", type = EditType.TEXTAREA)
    )
    private String remark;

    @Transient
    @Resource
    private NodeManager nodeManager;

    @Transient
    @Resource
    private EruptDao eruptDao;

    @Override
    public void afterUpdate(CloudNode cloudNode) {
        DataProxy.super.afterUpdate(cloudNode);
    }

    @Override
    public void beforeAdd(CloudNode cloudNode) {
        if (null == cloudNode.getAccessToken()) cloudNode.setAccessToken(Erupts.generateCode(16).toUpperCase());
    }

    @Override
    public void beforeUpdate(CloudNode cloudNode) {
        this.beforeAdd(cloudNode);
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> list) {
        for (Map<String, Object> map : list) {
            Optional.ofNullable(map.get(ACCESS_TOKEN)).ifPresent(it -> {
                String token = it.toString();
                map.put(ACCESS_TOKEN, token.substring(0, 3) + "******" + token.substring(token.length() - 3));
            });
            String eruptNumStr = "eruptNum";
            String instanceNumStr = "instanceNum";
            String version = "version";
            String eruptModuleNum = "eruptModuleNum";
            map.put(eruptNumStr, '-');
            map.put(instanceNumStr, '-');
            map.put(version, '-');
            map.put(eruptModuleNum, '-');
            try {
                MetaNode metaNode = nodeManager.getNode(map.get(NODE_NAME).toString());
                Optional.ofNullable(nodeManager.getNode(map.get(NODE_NAME).toString())).ifPresent(metaNode1 -> {
                    Function<Collection<String>, Object> function = (it) -> null == it ? 0 : String.format("<a href='javascript:alert(\"%s\");'>%d</a>",
                            String.join("\\u000a", it), it.size());
                    map.put(eruptNumStr, function.apply(metaNode.getErupts()));
                    map.put(instanceNumStr, function.apply(metaNode.getLocations()));
                    map.put(eruptModuleNum, function.apply(metaNode.getEruptModules()));
                    map.put(version, metaNode.getVersion());
                });
            } catch (Exception e) {
                map.put(version, String.format("<span style='color:#f00'>%s</span>", e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterDelete(CloudNode cloudNode) {
        nodeManager.removeNode(cloudNode.getNodeName());
    }

    @Override
    public List<String> fetchTags(String[] params) {
        return eruptDao.getJdbcTemplate().queryForList("select name from e_upms_user", String.class);
    }
}
