package xyz.erupt.eruptapi.controller;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by liyuepeng on 2019-08-14.
 */
@RestController
@RequestMapping("/open-sql-api")
public class OpenSqlApiController {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param fileName   文件名称
     * @param sqlElement xml中sql元素
     * @param pageSize   页面大小
     * @param pageIndex  索引开始值为1
     * @param request
     * @return 查询结果
     */
    @RequestMapping("/query/{fileName}/{sqlElement}")
    @ResponseBody
    public List query(@PathVariable("fileName") String fileName,
                      @PathVariable("sqlElement") String sqlElement,
                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                      @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                      HttpServletRequest request) {
        if (pageSize == null || pageSize > 100) {
            pageSize = 100;
        }
        if (pageIndex == null) {
            pageIndex = 1;
        }
        String sql = getSqlElement(fileName, sqlElement).getTextTrim();
        Query query = entityManager.createNativeQuery(sql);
        setSqlParam(request, query, sql);
        query.setMaxResults(pageSize);
        query.setFirstResult(pageIndex - 1);
        query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return query.getResultList();
    }

    @RequestMapping("/modify/{fileName}/{sqlElement}")
    @ResponseBody
    @Transactional
    public boolean update(@PathVariable("fileName") String fileName,
                          @PathVariable("sqlElement") String sqlElement,
                          HttpServletRequest request) {
        String sql = getSqlElement(fileName, sqlElement).getTextTrim();
        Query query = entityManager.createNativeQuery(sql);
        setSqlParam(request, query, sql);
        return query.executeUpdate() != 0;
    }


    private void setSqlParam(HttpServletRequest request, Query query, String sql) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (sql.contains(":" + parameterName)) {
                query.setParameter(parameterName, request.getParameter(parameterName));
            }
        }
    }


    private Element getSqlElement(String fileName, String sqlElement) {
        try {
            Resource resource = new ClassPathResource("sql/" + fileName + ".xml");
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(resource.getFile());
            return document.getRootElement().element(sqlElement);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


}
