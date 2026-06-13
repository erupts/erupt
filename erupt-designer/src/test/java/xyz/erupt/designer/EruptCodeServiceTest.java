package xyz.erupt.designer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import xyz.erupt.designer.pojo.DesignerForm;
import xyz.erupt.designer.service.EruptCodeService;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.linq.lambda.LambdaSee;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author YuePeng
 * date 2026-06-12
 */
public class EruptCodeServiceTest {

    private final EruptCodeService service = new EruptCodeService();

    private final Gson gson = new Gson();

    // 参照实体：类名 / 字段名一律由 .class 与 LambdaSee 推导，杜绝魔法字符串与两侧重复
    @Getter
    static class Brand extends BaseModel {
        private String name;
    }

    @Getter
    static class Goods extends BaseModel {
        private String name;
        private Double price;
        private Boolean status;
        private String type;
        private Brand brand;
        private Date createTime;
    }

    private DesignerForm.DesignerField field(String fieldName, String viewJson, String editJson) {
        DesignerForm.DesignerField field = new DesignerForm.DesignerField();
        field.setFieldName(fieldName);
        if (null != viewJson) field.setView(gson.fromJson(viewJson, JsonObject.class));
        if (null != editJson) field.setEdit(gson.fromJson(editJson, JsonObject.class));
        return field;
    }

    @Test
    public void generate() {
        DesignerForm form = new DesignerForm();
        form.setPkg("com.example.model");
        form.setClassName(Goods.class.getSimpleName());
        form.setExtendsModel(BaseModel.class.getSimpleName());
        form.setErupt(gson.fromJson("{name:'商品管理', power:{export:true}}", JsonObject.class));

        DesignerForm.DesignerField name = field(LambdaSee.field(Goods::getName),
                "{title:'商品名称', sortable:true}",
                "{title:'商品名称', notNull:true, search:{value:true, vague:true}, type:'INPUT', inputType:{length:100}}");
        DesignerForm.DesignerField price = field(LambdaSee.field(Goods::getPrice), "{title:'价格'}",
                "{title:'价格', type:'NUMBER', numberType:{min:0}}");
        price.setFieldType(Double.class.getSimpleName());
        DesignerForm.DesignerField status = field(LambdaSee.field(Goods::getStatus), "{title:'上架'}",
                "{title:'上架', type:'BOOLEAN', boolType:{trueText:'上架', falseText:'下架'}}");
        DesignerForm.DesignerField type = field(LambdaSee.field(Goods::getType), "{title:'分类'}",
                "{title:'分类', type:'CHOICE', choiceType:{vl:[{value:'1',label:'家电'},{value:'2',label:'数码'}]}}");
        DesignerForm.DesignerField brand = field(LambdaSee.field(Goods::getBrand), "{title:'品牌', column:'name'}",
                "{title:'品牌', type:'REFERENCE_TABLE'}");
        brand.setLinkErupt(Brand.class.getSimpleName());
        DesignerForm.DesignerField createTime = field(LambdaSee.field(Goods::getCreateTime), "{title:'创建时间'}",
                "{title:'创建时间', type:'DATE', dateType:{type:'DATE_TIME'}}");

        form.setFields(Arrays.asList(name, price, status, type, brand, createTime));
        String code = service.generate(form);
        System.out.println(code);

        // JavaPoet may break declaration annotations across lines; collapse whitespace so the
        // assertions below stay readable and check semantics, not the exact line layout.
        String flat = code.replaceAll("\\s+", " ").replace("( ", "(").replace(" )", ")");
        assertTrue(flat.contains("@Erupt(name = \"商品管理\", power = @Power(export = true))"));
        assertTrue(flat.contains("@Table(name = \"" + EruptCodeService.camelToUnderline(Goods.class.getSimpleName()) + "\")"));
        assertTrue(flat.contains("public class " + Goods.class.getSimpleName() + " extends " + BaseModel.class.getSimpleName() + " {"));
        assertTrue(flat.contains("edit = @Edit(title = \"价格\", type = EditType.NUMBER, numberType = @NumberType(min = 0))"));
        assertTrue(flat.contains("private " + Double.class.getSimpleName() + " " + LambdaSee.field(Goods::getPrice) + ";"));
        assertTrue(flat.contains("boolType = @BoolType(falseText = \"下架\", trueText = \"上架\")"));
        assertTrue(flat.contains("@VL(value = \"1\", label = \"家电\")"));
        assertTrue(flat.contains("@ManyToOne"));
        assertTrue(flat.contains("@JoinColumn(name = \"" + EruptCodeService.camelToUnderline(LambdaSee.field(Goods::getBrand)) + "_id\")"));
        assertTrue(flat.contains("private " + Brand.class.getSimpleName() + " " + LambdaSee.field(Goods::getBrand) + ";"));
        assertTrue(flat.contains("dateType = @DateType(type = DateType.Type.DATE_TIME)"));
        assertTrue(flat.contains("private " + Date.class.getSimpleName() + " " + LambdaSee.field(Goods::getCreateTime) + ";"));
        assertTrue(code.contains("import " + Date.class.getName() + ";"));
        // values equal to annotation defaults must not be emitted
        assertFalse(flat.contains("notNull = false"));
        // value=true equals Search's own default → omitted; vague=true differs → emitted
        assertTrue(flat.contains("search = @Search(vague = true)"));
    }

    @Test
    public void searchDefaultCollapsesToMarker() {
        DesignerForm form = new DesignerForm();
        form.setClassName(Goods.class.getSimpleName());
        // search = {value:true} differs from @Search(false) but every member equals Search's own
        // defaults, so it must collapse to `search = @Search`
        form.setFields(Collections.singletonList(
                field(LambdaSee.field(Goods::getName), null, "{title:'名称', search:{value:true}, type:'INPUT'}")));
        String code = service.generate(form);
        System.out.println(code);
        assertTrue(code.contains("search = @Search"));
        assertFalse(code.contains("search = @Search("));
    }

}
