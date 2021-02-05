package xyz.erupt.rent.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.rent.service.RentDataLoadService;

import javax.persistence.*;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2021/2/3 16:17
 */
@Erupt(name = "多租户配置", dataProxy = RentDataLoadService.class)
@Table(name = "e_rent", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mappingValue" }),
        @UniqueConstraint(columnNames = {"token" })
})
@Entity
@Getter
@Setter
public class EruptRent {

    public static final String MAPPING_DOMAIN = "domain";
    public static final String MAPPING_SECOND_DOMAIN = "secondDomain";
    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField
    private Long id;
    @EruptField(
            views = @View(title = "Token")
    )
    private String token;
    @EruptField(
            views = @View(title = "租户名称"),
            edit = @Edit(title = "租户名称", notNull = true, search = @Search(vague = true))
    )
    private String name;
    @EruptField(
            views = @View(title = "开始时间"),
            edit = @Edit(title = "开始时间", notNull = true, search = @Search(vague = true),
                    dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date start;
    @EruptField(
            views = @View(title = "结束时间"),
            edit = @Edit(title = "结束时间", notNull = true, search = @Search(vague = true),
                    dateType = @DateType(type = DateType.Type.DATE_TIME, pickerMode = DateType.PickerMode.FUTURE))
    )
    private Date end;
    @EruptField(
            views = @View(title = "映射方式"),
            edit = @Edit(title = "映射方式",
                    type = EditType.CHOICE,
                    search = @Search,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = "domain", label = "完整域名"),
                                    @VL(value = "secondDomain", label = "二级域名（三级）"),
                            }
                    )
            )
    )
    private String mappingType;

    @EruptField(
            views = @View(title = "租户域名"),
            edit = @Edit(title = "映射值", notNull = true, search = @Search,
                    desc = "例：abc.erupt.xyz")
    )
    private String mappingValue;

    @EruptField(
            views = @View(title = "状态", sortable = true),
            edit = @Edit(title = "状态", notNull = true, search = @Search, boolType = @BoolType(trueText = "激活", falseText = "锁定"))
    )
    private Boolean status;

    @EruptField(
            views = @View(title = "备注"),
            edit = @Edit(title = "备注")
    )
    private String remark;

}
