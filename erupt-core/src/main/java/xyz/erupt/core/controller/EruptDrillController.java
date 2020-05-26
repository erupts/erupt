package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.lang.reflect.Field;

/**
 * @author liyuepeng
 * @date 2020-03-06
 */
@RestController
@RequestMapping(RestPath.ERUPT_DATA)
public class EruptDrillController {

    @Autowired
    private EruptModifyController eruptModifyController;

    @Autowired
    private EruptService eruptService;

    @PostMapping("{erupt}/drill/{code}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Page drill(@PathVariable("erupt") String eruptName,
                      @PathVariable("code") String code,
                      @PathVariable("id") String id,
                      @RequestBody JsonObject searchCondition) throws IllegalAccessException {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        Link link = null;
        for (Drill drill : eruptModel.getErupt().drills()) {
            if (drill.code().equals(code)) {
                link = drill.link();
            }
        }
        if (null == link) {
            link = eruptModel.getErupt().tree().linkTable()[0];
        }
        if (null != link) {
            if (!eruptService.verifyIdPermissions(eruptModel, id)) {
                throw new EruptNoLegalPowerException();
            }
            Field field = ReflectUtil.findClassField(eruptModel.getClazz(), link.column());
            field.setAccessible(true);
            Object data = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));
            Object val = field.get(data);
            if (null == val) {
                return new Page();
            }
            return eruptService.getEruptData(
                    CoreService.getErupt(link.eruptClass().getSimpleName()),
                    searchCondition.remove(Page.PAGE_INDEX_STR).getAsInt(),
                    searchCondition.remove(Page.PAGE_SIZE_STR).getAsInt(),
                    searchCondition.remove(Page.PAGE_SORT_STR).getAsString(),
                    searchCondition, null, String.format("%s = '%s'",
                            link.eruptClass().getSimpleName() + "." + link.joinColumn(), val));
        }
        throw new EruptNoLegalPowerException();
    }

    @PostMapping("/add/{erupt}/drill/{code}/{id}")
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel drillAdd(@PathVariable("erupt") String erupt,
                                  @PathVariable("code") String code,
                                  @PathVariable("id") String id,
                                  @RequestBody JsonObject data) throws Exception {
        EruptModel eruptModel = CoreService.getErupt(erupt);
        Link link = null;
        for (Drill drill : eruptModel.getErupt().drills()) {
            if (code.equals(drill.code())) {
                link = drill.link();
            }
        }
        if (null == link) {
            link = eruptModel.getErupt().tree().linkTable()[0];
        }
        if (null != link) {
            if (!eruptService.verifyIdPermissions(eruptModel, id)) {
                throw new EruptNoLegalPowerException();
            }
            JsonObject jo = new JsonObject();
            String joinColumn = link.joinColumn();
            Field field = ReflectUtil.findClassField(eruptModel.getClazz(), link.column());
            field.setAccessible(true);
            Object val = field.get(AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz())
                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id)));
            if (joinColumn.contains(".")) {
                String[] jc = joinColumn.split("\\.");
                JsonObject jo2 = new JsonObject();
                jo2.addProperty(jc[1], val.toString());
                jo.add(jc[0], jo2);
            } else {
                jo.addProperty(joinColumn, val.toString());
            }
            return eruptModifyController.addEruptData(link.eruptClass().getSimpleName(), data, jo);
        }
        throw new EruptNoLegalPowerException();
    }


}
