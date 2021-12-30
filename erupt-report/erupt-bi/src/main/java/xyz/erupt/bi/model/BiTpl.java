//package xyz.erupt.bi.model;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.springframework.stereotype.Component;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.EruptI18n;
//import xyz.erupt.annotation.fun.DataProxy;
//import xyz.erupt.annotation.sub_field.Edit;
//import xyz.erupt.annotation.sub_field.EditType;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
//import xyz.erupt.jpa.model.MetaModelUpdateVo;
//
//import javax.persistence.Entity;
//import javax.persistence.Table;
//import javax.persistence.UniqueConstraint;
//
///**
// * @author YuePeng
// * date 2021/12/30 20:26
// */
//@Entity
//@Table(name = "e_bi_tpl", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
//@Erupt(name = "组件模板", orderBy = "createTime desc", dataProxy = BiTpl.class)
//@Getter
//@Setter
//@EruptI18n
//@Component
//public class BiTpl extends MetaModelUpdateVo implements DataProxy<BiTpl> {
//
//    @EruptField(
//            views = @View(title = "编码", width = "80px")
//    )
//    private String code;
//
//    @EruptField(
//            views = @View(title = "名称"),
//            edit = @Edit(title = "名称", notNull = true)
//    )
//    private String name;
//
//    @EruptField(
//            views = @View(title = "模板"),
//            edit = @Edit(title = "模板", desc = "语法Freemarker", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "html"))
//    )
//    private String tpl;
//
//    @Override
//    public void beforeAdd(BiTpl biTpl) {
//        biTpl.setCode(RandomStringUtils.randomAlphabetic(6));
//    }
//}
