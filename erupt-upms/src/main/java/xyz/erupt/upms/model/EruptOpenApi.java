//package xyz.erupt.upms.model;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.apache.commons.lang3.RandomStringUtils;
//import xyz.erupt.annotation.Erupt;
//import xyz.erupt.annotation.EruptField;
//import xyz.erupt.annotation.EruptI18n;
//import xyz.erupt.annotation.fun.DataProxy;
//import xyz.erupt.annotation.sub_field.Edit;
//import xyz.erupt.annotation.sub_field.EditType;
//import xyz.erupt.annotation.sub_field.View;
//import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
//import xyz.erupt.annotation.sub_field.sub_edit.Search;
//import xyz.erupt.jpa.model.MetaModelUpdateVo;
//
//import javax.persistence.Entity;
//import javax.persistence.ManyToOne;
//import javax.persistence.Table;
//
///**
// * @author YuePeng
// * date 2022/10/25 20:31
// */
//@Entity
//@Table(name = "e_open_api")
//@Erupt(
//        name = "开放访问erupt接口",
//        dataProxy = EruptOpenApi.class
//)
//@EruptI18n
//@Getter
//@Setter
//public class EruptOpenApi extends MetaModelUpdateVo implements DataProxy<EruptOpenApi> {
//
//    @EruptField(
//            views = @View(title = "应用名称", sortable = true),
//            edit = @Edit(title = "应用名称", notNull = true, search = @Search(vague = true))
//    )
//    private String name;
//
//    @EruptField(
//            views = @View(title = "APPID", desc = "应用唯一标识", sortable = true)
//    )
//    private String appid;
//
//    @EruptField(
//            views = @View(title = "Token 有效期", sortable = true),
//            edit = @Edit(title = "Token 有效期", desc = "单位：分钟，0表示永不过期", notNull = true)
//    )
//    private Integer expire = 0;
//
//    @ManyToOne
//    @EruptField(
//            views = @View(title = "绑定用户权限", column = "name"),
//            edit = @Edit(title = "绑定用户权限", type = EditType.REFERENCE_TABLE, notNull = true)
//    )
//    private EruptUser eruptUser;
//
//    @EruptField(
//            views = @View(title = "状态", sortable = true),
//            edit = @Edit(
//                    title = "状态", search = @Search, type = EditType.BOOLEAN, notNull = true,
//                    boolType = @BoolType(trueText = "激活", falseText = "锁定")
//            )
//    )
//    private Boolean status = true;
//
//    @Override
//    public void beforeAdd(EruptOpenApi eruptOpenApi) {
//        eruptOpenApi.setAppid(RandomStringUtils.randomAlphanumeric(12));
//    }
//
//}
