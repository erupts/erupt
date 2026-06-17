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

    // Reference entities: class name / field name always derived from .class and LambdaSee — no magic strings
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
        form.setErupt(gson.fromJson("{name:'Product Management', power:{export:true}}", JsonObject.class));

        DesignerForm.DesignerField name = field(LambdaSee.field(Goods::getName),
                "{title:'Product Name', sortable:true}",
                "{title:'Product Name', notNull:true, search:{value:true, vague:true}, type:'INPUT', inputType:{length:100}}");
        DesignerForm.DesignerField price = field(LambdaSee.field(Goods::getPrice), "{title:'Price'}",
                "{title:'Price', type:'NUMBER', numberType:{min:0}}");
        price.setFieldType(Double.class.getSimpleName());
        DesignerForm.DesignerField status = field(LambdaSee.field(Goods::getStatus), "{title:'Listed'}",
                "{title:'Listed', type:'BOOLEAN', boolType:{trueText:'Listed', falseText:'Unlisted'}}");
        DesignerForm.DesignerField type = field(LambdaSee.field(Goods::getType), "{title:'Category'}",
                "{title:'Category', type:'CHOICE', choiceType:{vl:[{value:'1',label:'Electronics'},{value:'2',label:'Digital'}]}}");
        DesignerForm.DesignerField brand = field(LambdaSee.field(Goods::getBrand), "{title:'Brand', column:'name'}",
                "{title:'Brand', type:'REFERENCE_TABLE'}");
        brand.setLinkErupt(Brand.class.getSimpleName());
        DesignerForm.DesignerField createTime = field(LambdaSee.field(Goods::getCreateTime), "{title:'Created Time'}",
                "{title:'Created Time', type:'DATE', dateType:{type:'DATE_TIME'}}");

        form.setFields(Arrays.asList(name, price, status, type, brand, createTime));
        String code = service.generate(form);
        System.out.println(code);

        // JavaPoet may break declaration annotations across lines; collapse whitespace so the
        // assertions below stay readable and check semantics, not the exact line layout.
        String flat = code.replaceAll("\\s+", " ").replace("( ", "(").replace(" )", ")");
        assertTrue(flat.contains("@Erupt(name = \"Product Management\", power = @Power(export = true))"));
        assertTrue(flat.contains("@Table(name = \"" + EruptCodeService.camelToUnderline(Goods.class.getSimpleName()) + "\")"));
        assertTrue(flat.contains("public class " + Goods.class.getSimpleName() + " extends " + BaseModel.class.getSimpleName() + " {"));
        assertTrue(flat.contains("edit = @Edit(title = \"Price\", type = EditType.NUMBER, numberType = @NumberType(min = 0))"));
        assertTrue(flat.contains("private " + Double.class.getSimpleName() + " " + LambdaSee.field(Goods::getPrice) + ";"));
        assertTrue(flat.contains("boolType = @BoolType(falseText = \"Unlisted\", trueText = \"Listed\")"));
        assertTrue(flat.contains("@VL(value = \"1\", label = \"Electronics\")"));
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
                field(LambdaSee.field(Goods::getName), null, "{title:'Name', search:{value:true}, type:'INPUT'}")));
        String code = service.generate(form);
        System.out.println(code);
        assertTrue(code.contains("search = @Search"));
        assertFalse(code.contains("search = @Search("));
    }

}
