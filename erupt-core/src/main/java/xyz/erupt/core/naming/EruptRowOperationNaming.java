package xyz.erupt.core.naming;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.controller.EruptDataController;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.view.EruptModel;

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
        // remote (erupt-cloud node) erupt is not registered locally; still record the audit
        // on the server, degraded to the operation code carried in the request path
        if (null == erupt) {
            return menuName + " | " + rowOperationCode();
        }
        return findRowOperation(erupt).title() + " | " + erupt.getErupt().name();
    }

    private RowOperation findRowOperation(EruptModel eruptModel) {
        String code = rowOperationCode();
        return Arrays.stream(eruptModel.getErupt().rowOperation())
                .filter(operation -> operation.code().equals(code)).findFirst()
                .orElseThrow(() -> new RuntimeException(eruptModel.getEruptName() + " RowOperation not found " + code));
    }

    // The operation code segment of the current request path, e.g. /operator/{code}
    private String rowOperationCode() {
        return request.getServletPath().split(EruptDataController.OPERATOR_PATH_STR + "/")[1];
    }
}