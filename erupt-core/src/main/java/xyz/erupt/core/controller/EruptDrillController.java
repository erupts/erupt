package xyz.erupt.core.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.context.MetaErupt;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.naming.EruptRecordNaming;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2020-03-06
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA)
@RequiredArgsConstructor
public class EruptDrillController {

    private final EruptModifyController eruptModifyController;

    private final EruptService eruptService;

    @PostMapping("{erupt}/drill/{code}/{id}")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    public Page drill(@PathVariable("erupt") String eruptName,
                      @PathVariable("code") String code,
                      @PathVariable("id") String id,
                      @RequestBody TableQueryVo tableQueryVo) throws IllegalAccessException {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Link link = findDrillLink(eruptModel.getErupt(), code);
        eruptService.verifyIdPermissions(eruptModel, id);
        Field field = ReflectUtil.findClassField(eruptModel.getClazz(), link.column());
        Object data = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));
        field.setAccessible(true);
        Object val = field.get(data);
        if (null == val) return new Page();
        MetaContext.register(new MetaErupt(link.linkErupt().getSimpleName()));
        return eruptService.getEruptData(
                EruptCoreService.getErupt(link.linkErupt().getSimpleName()), tableQueryVo, null,
                String.format("%s = '%s'", link.linkErupt().getSimpleName() + "." + link.joinColumn(), val)
        );
    }

    @PostMapping("/add/{erupt}/drill/{code}/{id}")
    @EruptRecordOperate(value = "新增", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel drillAdd(@PathVariable("erupt") String erupt, @PathVariable("code") String code,
                                  @PathVariable("id") String id, @RequestBody JsonObject data) throws Exception {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Link link = findDrillLink(eruptModel.getErupt(), code);
        eruptService.verifyIdPermissions(eruptModel, id);
        Map<String, Object> extraData = new HashMap<>();
        String joinColumn = link.joinColumn();
        Field field = ReflectUtil.findClassField(eruptModel.getClazz(), link.column());
        field.setAccessible(true);
        Object val = field.get(DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id)));
        if (joinColumn.contains(".")) {
            String[] jc = joinColumn.split("\\.");
            JsonObject jo2 = new JsonObject();
            jo2.addProperty(jc[1], val.toString());
            extraData.put(jc[0], jo2);
        } else {
            extraData.put(joinColumn, val.toString());
        }
        MetaContext.register(new MetaErupt(link.linkErupt().getSimpleName()));
        return eruptModifyController.addEruptData(link.linkErupt().getSimpleName(), data, extraData);
    }

    private Link findDrillLink(Erupt erupt, String code) {
        return Stream.of(erupt.drills()).filter(it -> code.equals(it.code()))
                .findFirst().orElseThrow(EruptNoLegalPowerException::new).link();
    }


}
