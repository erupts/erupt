package xyz.erupt.core.naming;

import org.springframework.stereotype.Component;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author YuePeng
 * date 2021/5/7 10:28
 */
@Component
public class EruptRowOperationConfig implements EruptRecordOperate.DynamicConfig {

    @Resource
    private HttpServletRequest request;

    @Override
    public String naming(String desc, String eruptName, Method method) {
        EruptModel erupt = EruptCoreService.getErupt(eruptName);
        RowOperation operation = findRowOperation(erupt);
        return erupt.getErupt().name() + " | " + operation.title();
    }

    private RowOperation findRowOperation(EruptModel eruptModel) {
        String code = request.getServletPath().split(EruptDataController.OPERATOR_PATH_STR + "/")[1];
        return Arrays.stream(eruptModel.getErupt().rowOperation())
                .filter(operation -> operation.code().equals(code)).findFirst()
                .orElseThrow(() -> new RuntimeException(eruptModel.getEruptName() + " RowOperation not found " + code));
    }
}