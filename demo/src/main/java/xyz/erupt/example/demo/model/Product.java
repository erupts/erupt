package xyz.erupt.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;

import javax.persistence.Table;
import javax.persistence.*;
import java.util.Set;

/**
 * Created by liyuepeng on 2019-07-29.
 */
@Erupt(
        name = "产品",
        dataProxy = Product.class
)
@Entity
@Table(name = "PRODUCT")
@Getter
@Setter
public class Product implements DataProxy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @EruptField
    private Long id;


//    @ManyToOne
//    @JoinColumn(name = "USER_ID")
//    @EruptField(
//            views = {
//                    @View(title = "姓名", column = "name"),
//                    @View(title = "用户名", column = "account")
//            },
//            edit = @Edit(
//                    title = "用户",
//                    type = EditType.REFERENCE_TABLE,
//                    search = @Search(value = true)
//            )
//    )
//    private EruptUser eruptUser;

    @Column(name = "NAME")
    @EruptField(
            views = @View(title = "产品名称", sortable = true),
            edit = @Edit(title = "产品名称", notNull = true, search = @Search(true))
    )
    private String name = "xxx";

//    @Column(name = "REMARK")
//    @EruptField(
//            views = @View(title = "产品描述", sortable = true),
//            edit = @Edit(title = "产品描述", notNull = true, search = @Search(true))
//    )
//    private String remark;

    @Column(name = "NUMBER")
    @EruptField(
            views = @View(title = "数字", sortable = true),
            edit = @Edit(title = "数字", notNull = true, search = @Search(true))
    )
    private Integer number;

    @Column(name = "BOOLEAN")
    @EruptField(
            views = @View(title = "布尔", sortable = true),
            edit = @Edit(title = "布尔", notNull = true, search = @Search(true), type = EditType.BOOLEAN)
    )
    private Boolean bool;

    @Column(name = "CHOICE")
    @EruptField(
            views = @View(title = "选择", sortable = true),
            edit = @Edit(title = "选择", notNull = true, search = @Search(true),
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(label = "erupt", value = "1"),
                            @VL(label = "yuePeng", value = "2"),
                            @VL(label = "English", value = "3")
                    })
            )
    )
    private Integer choice;


    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL})
    @EruptField(
            edit = @Edit(
                    title = "参数列表",
                    type = EditType.TAB_TABLE_ADD
            )
    )
    private Set<ProductParam> productParam;

    @Override
    public void excelExport(Workbook wb) {
        Sheet sheet = wb.getSheetAt(0);
        CellStyle style = wb.createCellStyle();
        style.setLeftBorderColor(BorderStyle.DOUBLE.getCode());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.YELLOW.index);
        Cell cell = sheet.getRow(2).getCell(1);
        cell.setCellValue("@@#####$$$$$$");
        cell.setCellStyle(style);
    }
}
