package xyz.erupt.upms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

/**
 * @author YuePeng
 * date 2022/10/25 20:31
 */
@Entity
@Table(name = "e_open_api")
@Erupt(
        name = "Open API",
        dataProxy = EruptOpenApiDataProxy.class,
        rowOperation = @RowOperation(title = "Update Secret Key", icon = "fa fa-refresh", operationHandler = EruptOpenApiDataProxy.class, mode = RowOperation.Mode.SINGLE)
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
            views = @View(title = "name", sortable = true),
            edit = @Edit(title = "name", notNull = true, search = @Search)
    )
    private String name;

    @EruptField(
            views = @View(title = "Token Validity Period", desc = "minutes", sortable = true),
            edit = @Edit(title = "Token Validity Period", desc = "minutes", numberType = @NumberType(min = 1), notNull = true)
    )
    private Integer expire = 3600;

    @ManyToOne
    @EruptField(
            views = @View(title = "Bind User Permissions", column = "name"),
            edit = @Edit(title = "Bind User Permissions", type = EditType.REFERENCE_TABLE, notNull = true)
    )
    private EruptUser eruptUser;

    @EruptField(
            views = @View(title = "status", sortable = true),
            edit = @Edit(
                    title = "status", search = @Search, type = EditType.BOOLEAN, notNull = true,
                    boolType = @BoolType(trueText = "Activate", falseText = "Locked")
            )
    )
    private Boolean status = true;

    @EruptField(
            sort = 2000,
            views = @View(title = "Secret Key"),
            edit = @Edit(title = "Secret Key", readonly = @Readonly, inputType = @InputType(fullSpan = true))
    )
    private String secret;

    private String currentToken;

}
