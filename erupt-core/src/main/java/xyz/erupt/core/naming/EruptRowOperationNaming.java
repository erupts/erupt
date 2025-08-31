package xyz.erupt.core.naming;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author YuePeng
 * date 2021/5/7 10:28
 */
@Component
public class EruptRowOperationNaming implements EruptRecordOperate.DynamicConfig {

    @Resource
    private HttpServletRequest request;

    @Override
    public String naming(String desc, String menuName, String eruptName, Method method) {
        EruptModel erupt = EruptCoreService.getErupt(eruptName);
        if (null == erupt) {
            return menuName + " | @RowOperation";
        }
        return findRowOperation(erupt).title() + " | " + erupt.getErupt().name();
    }

    private RowOperation findRowOperation(EruptModel eruptModel) {
        String code = request.getServletPath().split(EruptDataController.OPERATOR_PATH_STR + "/")[1];
        return Arrays.stream(eruptModel.getErupt().rowOperation())
                .filter(operation -> operation.code().equals(code)).findFirst()
                .orElseThrow(() -> new RuntimeException(eruptModel.getEruptName() + " RowOperation not found " + code));
    }
}