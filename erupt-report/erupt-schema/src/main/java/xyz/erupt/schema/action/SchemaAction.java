package xyz.erupt.schema.action;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.schema.model.SchemaModel;
import xyz.erupt.tpl.annotation.EruptTpl;
import xyz.erupt.tpl.annotation.TplAction;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptContextService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2020-02-24
 */
@Service
@EruptTpl(engine = Tpl.Engine.FreeMarker)
public class SchemaAction {

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptContextService eruptContextService;

    @TplAction("amis.schema.html")
    public Map<String, Object> render() throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("json", "{}");
        EruptMenu eruptMenu = eruptContextService.getCurrentEruptMenu();
        SchemaModel schemaAction = eruptDao.queryEntity(SchemaModel.class, "code='" + eruptMenu.getCode() + "'");
        if (null == schemaAction) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.sendError(HttpStatus.NOT_FOUND.value());
            return result;
        }
        result.put("json", schemaAction.getJson());
        return result;
    }

}
