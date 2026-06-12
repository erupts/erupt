package xyz.erupt.designer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import xyz.erupt.designer.model.DesignerForm;
import xyz.erupt.designer.service.EruptCodeService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author YuePeng
 * date 2026-06-12
 */
public class EruptCodeServiceTest {

    private final EruptCodeService service = new EruptCodeService();

    private final Gson gson = new Gson();

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
        form.setClassName("Goods");
        form.setExtendsModel("BaseModel");
        form.setErupt(gson.fromJson("{name:'商品管理', power:{export:true}}", JsonObject.class));

        DesignerForm.DesignerField name = field("name",
                "{title:'商品名称', sortable:true}",
                "{title:'商品名称', notNull:true, search:{value:true, vague:true}, type:'INPUT', inputType:{length:100}}");
        DesignerForm.DesignerField price = field("price", "{title:'价格'}",
                "{title:'价格', type:'NUMBER', numberType:{min:0}}");
        price.setFieldType("Double");
        DesignerForm.DesignerField status = field("status", "{title:'上架'}",
                "{title:'上架', type:'BOOLEAN', boolType:{trueText:'上架', falseText:'下架'}}");
        DesignerForm.DesignerField type = field("type", "{title:'分类'}",
                "{title:'分类', type:'CHOICE', choiceType:{vl:[{value:'1',label:'家电'},{value:'2',label:'数码'}]}}");
        DesignerForm.DesignerField brand = field("brand", "{title:'品牌', column:'name'}",
                "{title:'品牌', type:'REFERENCE_TABLE'}");
        brand.setLinkErupt("Brand");
        DesignerForm.DesignerField createTime = field("createTime", "{title:'创建时间'}",
                "{title:'创建时间', type:'DATE', dateType:{type:'DATE_TIME'}}");

        form.setFields(Arrays.asList(name, price, status, type, brand, createTime));
        String code = service.generate(form);
        System.out.println(code);

        assertTrue(code.contains("@Erupt(name = \"商品管理\", power = @Power(export = true))"));
        assertTrue(code.contains("@Table(name = \"goods\")"));
        assertTrue(code.contains("public class Goods extends BaseModel {"));
        assertTrue(code.contains("edit = @Edit(title = \"价格\", type = EditType.NUMBER, numberType = @NumberType(min = 0))"));
        assertTrue(code.contains("private Double price;"));
        assertTrue(code.contains("boolType = @BoolType(falseText = \"下架\", trueText = \"上架\")"));
        assertTrue(code.contains("@VL(value = \"1\", label = \"家电\")"));
        assertTrue(code.contains("@ManyToOne"));
        assertTrue(code.contains("@JoinColumn(name = \"brand_id\")"));
        assertTrue(code.contains("private Brand brand;"));
        assertTrue(code.contains("dateType = @DateType(type = DateType.Type.DATE_TIME)"));
        assertTrue(code.contains("private Date createTime;"));
        assertTrue(code.contains("import java.util.Date;"));
        // values equal to annotation defaults must not be emitted
        assertFalse(code.contains("notNull = false"));
        // value=true equals Search's own default → omitted; vague=true differs → emitted
        assertTrue(code.contains("search = @Search(vague = true)"));
    }

    @Test
    public void searchDefaultCollapsesToMarker() {
        DesignerForm form = new DesignerForm();
        form.setClassName("Demo");
        // search = {value:true} differs from @Search(false) but every member equals Search's own
        // defaults, so it must collapse to `search = @Search`
        form.setFields(java.util.Collections.singletonList(
                field("name", null, "{title:'名称', search:{value:true}, type:'INPUT'}")));
        String code = service.generate(form);
        System.out.println(code);
        assertTrue(code.contains("search = @Search"));
        assertFalse(code.contains("search = @Search("));
    }

}
