package xyz.erupt.bi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ButtonType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.Dynamic;
import xyz.erupt.bi.constant.DBTypeEnum;
import xyz.erupt.bi.handler.DataSourceTestButtonHandler;
import xyz.erupt.bi.handler.DriverChoice;
import xyz.erupt.bi.service.BiDataSourceService;
import xyz.erupt.jpa.model.MetaModelUpdateVo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_datasource", uniqueConstraints = @UniqueConstraint(name = "uk_bi_datasource_code", columnNames = "code"))
@Erupt(name = "Data Source Manager", dataProxy = BiDataSourceService.class)
@Getter
@Setter
@EruptI18n
public class BiDataSource extends MetaModelUpdateVo implements ChoiceFetchHandler<Void> {

    @Column(length = AnnotationConst.CODE_LENGTH)
    @EruptField(
            views = @View(title = "Code", sortable = true, width = "100px")
    )
    private String code;

    @EruptField(
            views = @View(title = "Name", sortable = true),
            edit = @Edit(title = "Name", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(title = "Driver", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = DriverChoice.class))
    )
    private String driver;

    @EruptField(
            views = @View(title = "DB Type", sortable = true),
            edit = @Edit(title = "DB Type", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = BiDataSource.class))
    )
    private String type;

    @EruptField(
            views = @View(title = "Connection String", type = ViewType.HTML),
            edit = @Edit(title = "Connection String", type = EditType.TEXTAREA, notNull = true)
    )
    private String url;

    @EruptField(
            views = @View(title = "Username", sortable = true),
            edit = @Edit(title = "Username")
    )
    private String userName;

    @EruptField(
            views = @View(title = "Password"),
            edit = @Edit(title = "Password", type = EditType.PASSWORD)
    )
    private String password;

    @Transient
    @EruptField(
            edit = @Edit(title = "Test Connection", type = EditType.BUTTON,
                    buttonType = @ButtonType(icon = "fa fa-plug", handler = DataSourceTestButtonHandler.class))
    )
    private String testConnection;

    @Column(length = AnnotationConst.CONFIG_LENGTH)
    @EruptField(
            edit = @Edit(title = "Pagination SQL", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql"),
                    placeHolder = DBTypeEnum.GENERAL_LIMIT,
                    desc = """
                            Pagination variables:
                            @sql: SQL statement
                            @size: page size
                            @skip: rows to skip
                            @sort: field sorting""",
                    dynamic = @Dynamic(dependField = "type", condition = "value === 'Other'")
            )
    )
    private String limitSql;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(title = "Connection Pool Config", desc = "Reference hikari, format: key=value", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "ini"))
    )
    private String poolConfig;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "Remark", type = ViewType.HTML),
            edit = @Edit(title = "Remark", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public List<VLModel> fetch(String[] params) {
        return Stream.of(DBTypeEnum.values()).map(it -> new VLModel(it.name(), it.name())).collect(Collectors.toList());
    }
}
