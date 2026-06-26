package xyz.erupt.core.controller.vis;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import java.lang.reflect.Field;

/**
 * @author YuePeng
 * date 2026/6/2
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY + "/calendar")
@RequiredArgsConstructor
public class EruptCalendarController {

    @PostMapping("/{erupt}/update_date")
    @EruptRouter(skipAuthIndex = 4, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    @SneakyThrows
    public R<Void> updateDate(@PathVariable String erupt, @RequestBody VisCalendarDateCommand command) {
        EruptModel eruptModel = EruptCoreService.getErupt(erupt);
        Erupts.powerLegal(eruptModel, PowerObject::isEdit);
        for (Vis vis : eruptModel.getErupt().vis()) {
            if (vis.code().equals(command.getVisCode())) {
                Object obj = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, command.getPk());
                Field dateField = ReflectUtil.findClassField(obj.getClass(), vis.calendarView().dateField());
                dateField.set(obj, DateUtil.getDate(dateField.getType(), command.getDate()));
                String endDateField = vis.calendarView().endDateField();
                if (!endDateField.isEmpty() && command.getEndDate() != null) {
                    Field endField = ReflectUtil.findClassField(obj.getClass(), endDateField);
                    endField.set(obj, DateUtil.getDate(endField.getType(), command.getEndDate()));
                }
                DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));
                DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
                DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));
                break;
            }
        }
        return R.ok();
    }

    @Getter
    @Setter
    public static class VisCalendarDateCommand {

        private String visCode;

        private Object pk;

        private String date;

        private String endDate;

    }

}
