package xyz.erupt.upms.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.NumberType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.data_proxy.EruptOpenApiDataProxy;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author YuePeng
 * date 2022/10/25 20:31
 */
@Entity
@Table(name = "e_open_api")
@Erupt(
        name = "Open API",
        dataProxy = EruptOpenApiDataProxy.class,
        rowOperation = @RowOperation(title = "更新秘钥", icon = "fa fa-refresh", operationHandler = EruptOpenApiDataProxy.class, mode = RowOperation.Mode.SINGLE)
)
@EruptI18n
@Getter
@Setter
public class EruptOpenApi extends MetaModelUpdateVo {

    @EruptField(
            views = @View(title = "APPID", sortable = true)
    )
    private String appid;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            views = @View(title = "Token有效期", desc = "minutes", sortable = true),
            edit = @Edit(title = "Token有效期", desc = "minutes", numberType = @NumberType(min = 1), notNull = true)
    )
    private Integer expire = 3600;

    @ManyToOne
    @EruptField(
            views = @View(title = "绑定用户权限", column = "name"),
            edit = @Edit(title = "绑定用户权限", type = EditType.REFERENCE_TABLE, notNull = true)
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "状态", sortable = true),
            edit = @Edit(
                    title = "状态", search = @Search, type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "激活", falseText = "锁定")
            )
    )
    private Boolean status = true;

    @EruptField(
            sort = 2000,
            views = @View(title = "秘钥"),
            edit = @Edit(title = "秘钥", readonly = @Readonly, inputType = @InputType(fullSpan = true))
    )
    private String secret;

    private String currentToken;

}
