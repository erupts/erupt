package xyz.erupt.core.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.annotation.sub_field.sub_edit.AutoCompleteType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.view.EruptFieldModel;

import java.util.List;

/**
 * @author liyuepeng
 * @date 2020-08-14
 */
@RestController
@RequestMapping(RestPath.ERUPT_COMP)
public class EruptComponentController {

    @RequestMapping("/auto-complete/{erupt}/{field}/{val}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public List<Object> findAutoCompleteValue(@PathVariable("erupt") String eruptName,
                                              @PathVariable("field") String field,
                                              @PathVariable("val") String val) {
        EruptFieldModel fieldModel = EruptCoreService.getErupt(eruptName).getEruptFieldMap().get(field);
        if (null == fieldModel) {
            throw new EruptNoLegalPowerException();
        }
        AutoCompleteType autoCompleteType = fieldModel.getEruptField().edit().autoCompleteType();
        if (val.length() < autoCompleteType.triggerLength()) {
            throw new RuntimeException("char length must >= " + autoCompleteType.triggerLength());
        }
        return EruptSpringUtil.getBean(autoCompleteType.handler()).completeHandler(val, autoCompleteType.param());
    }

}
