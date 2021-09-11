package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ShowBy;
import xyz.erupt.bi.constant.DBTypeEnum;
import xyz.erupt.bi.service.BiDataSourceService;
import xyz.erupt.upms.model.base.HyperModel;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi_datasource", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "数据源", dataProxy = BiDataSourceService.class)
@Getter
@Setter
public class BiDataSource extends HyperModel implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "编码", sortable = true),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            edit = @Edit(title = "驱动", notNull = true)
    )
    private String driver;

    @EruptField(
            views = @View(title = "连接字符串"),
            edit = @Edit(title = "连接字符串", type = EditType.TEXTAREA, notNull = true)
    )
    private String url;

    @EruptField(
            views = @View(title = "用户名", sortable = true),
            edit = @Edit(title = "用户名", notNull = true)
    )
    private String userName;

    @EruptField(
            edit = @Edit(title = "密码", inputType = @InputType(type = "password"))
    )
    private String password;

    @EruptField(
            views = @View(title = "数据库类型", sortable = true),
            edit = @Edit(title = "数据库类型", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = BiDataSource.class))
    )
    private String type;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            edit = @Edit(title = "分页语句", type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql"),
                    placeHolder = DBTypeEnum.GENERAL_LIMIT,
                    desc = "分页变量：\n" +
                            "@sql：sql语句\n" +
                            "@size：展示条数\n" +
                            "@skip：跳过行数",
                    showBy = @ShowBy(dependField = "type", expr = "value === 'Other'")
            )
    )
    private String limitSql;


    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            views = @View(title = "备注", type = ViewType.HTML),
            edit = @Edit(title = "备注", type = EditType.TEXTAREA)
    )
    private String remark;

    @Override
    public List<VLModel> fetch(String[] params) {
        return Stream.of(DBTypeEnum.values()).map(it -> new VLModel(it.name(), it.name())).collect(Collectors.toList());
    }
}
