package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.auth.model.base.HyperModel;
import xyz.erupt.bi.service.BiDataSourceService;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@Entity
@Table(name = "E_BI_DATASOURCE", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "数据源", dataProxy = BiDataSourceService.class)
@Getter
@Setter
public class BiDataSource extends HyperModel implements ChoiceFetchHandler {

    @EruptField(
            views = @View(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @Transient
    @EruptField(
            views = @View(title = "数据库类型"),
            edit = @Edit(title = "数据库类型", notNull = true, type = EditType.CHOICE,
                    choiceType = @ChoiceType(fetchHandler = BiDataSource.class))
    )
    private String db;

    @EruptField(
            views = @View(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            views = @View(title = "驱动"),
            edit = @Edit(title = "驱动", notNull = true)
    )
    private String driver;

    @EruptField(
            views = @View(title = "连接字符串"),
            edit = @Edit(title = "连接字符串", type = EditType.TEXTAREA, notNull = true)
    )
    private String url;

//    @EruptField(
//            views = @View(title = "类型"),
//            edit = @Edit(title = "类型", type = EditType.CHOICE, notNull = true,
//                    choiceType = @ChoiceType(vl = {
//                            @VL(value = "mysql", label = "mysql")
//                    }))
//    )
//    private String dbType;

    @EruptField(
            views = @View(title = "用户名"),
            edit = @Edit(title = "用户名", notNull = true)
    )
    private String userName;

    @EruptField(
            edit = @Edit(title = "密码")
    )
    private String password;

    @Override
    public Map<String, String> fetch(String[] params) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("mysql", "mysql");
//        for (Database value : Database.values()) {
//            map.put(value.name(), value.name().toLowerCase());
//        }
        return map;
    }
}
