package xyz.erupt.core.service;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2024/12/12 21:09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EruptModifyService {

    private final EruptService eruptService;

    private final HttpServletRequest request;

    @SneakyThrows
    public Object eruptInsertDataProcess(EruptModel eruptModel, JsonObject data) {
        Map<String, Object> extraData = new HashMap<>();
        this.setLinkValue(eruptModel, extraData);
        this.setDrillValue(eruptModel, extraData);
        return EruptUtil.jsonToEruptEntity(eruptModel, data, extraData);
    }

    private void setLinkValue(EruptModel eruptModel, Map<String, Object> extraData) {
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field()) && dependTree.dependNode()) {
            String linkVal = request.getHeader("link");
            //必须是强依赖才会自动注入值
            if (dependTree.dependNode()) {
                if (StringUtils.isBlank(linkVal)) {
                    throw new EruptWebApiRuntimeException("Place select tree node");
                } else {
                    String rm = ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName();
                    JsonObject sub = new JsonObject();
                    sub.addProperty(EruptCoreService.getErupt(rm).getErupt().primaryKeyCol(), linkVal);
                    extraData.put(dependTree.field(), sub);
                }
            }
        }
    }

    private void setDrillValue(EruptModel eruptModel, Map<String, Object> extraData) {
        eruptService.drillProcess(eruptModel, (link, val) -> {
            String joinColumn = link.joinColumn();
            if (joinColumn.contains(EruptConst.DOT)) {
                String[] jc = joinColumn.split("\\.");
                JsonObject jo2 = new JsonObject();
                jo2.addProperty(jc[1], val.toString());
                extraData.put(jc[0], jo2);
            } else {
                extraData.put(joinColumn, val.toString());
            }
        });
    }

    public void modifyLog(EruptModel eruptModel, String placeholder, String content) {
        log.info("[{} -> {}]:{}", eruptModel.getEruptName(), placeholder, content);
    }

}
