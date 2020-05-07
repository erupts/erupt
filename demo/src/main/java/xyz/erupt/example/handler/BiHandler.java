package xyz.erupt.example.handler;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.auth.service.EruptUserService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author liyuepeng
 * @date 2020-04-20
 */
@Service
public class BiHandler implements xyz.erupt.bi.fun.BiHandler {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EruptUserService eruptUserService;

    @Override
    public String exprHandler(String param, String expr) {
        return expr;
    }

    @Override
    public void resultHandler(String param, List<Map<String, Object>> result) {
        System.out.println(result);
    }

    @Override
    public void exportHandler(Map<String, Object> condition, Workbook workbook) {
        System.out.println(condition);
    }
}
