package xyz.erupt.erupt_open_api.controller;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.erupt_open_api.constant.RootElementAttribute;
import xyz.erupt.erupt_open_api.handler.SqlHandler;
import xyz.erupt.eruptcommon.util.SpringUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by liyuepeng on 2019-08-14.
 */
@RestController
@RequestMapping("/open-api/sql")
public class OpenSqlApiController {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${openApi.hotReadSqlXml:false}")
    private boolean hotReadSqlXml;

    private Map<String, Document> xmlDocuments = new HashMap<>();

    /**
     * @param fileName   文件名称
     * @param sqlElement xml中sql元素
     * @return 查询结果
     */
    @RequestMapping("/query/{fileName}/{sqlElement}")
    @ResponseBody
    public Object query(@PathVariable("fileName") String fileName,
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
        final int ps = pageSize;
        final int pi = pageIndex;
        return xmlToQuery(fileName, sqlElement, request, (query) -> {
            query.setMaxResults(ps);
            query.setFirstResult(pi - 1);
            query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            return query.getResultList();
        });
    }

    @RequestMapping("/modify/{fileName}/{sqlElement}")
    @ResponseBody
    @Transactional
    public Object modify(@PathVariable("fileName") String fileName,
                         @PathVariable("sqlElement") String sqlElement,
                         HttpServletRequest request) {
        return xmlToQuery(fileName, sqlElement, request, query -> query.executeUpdate() != 0);
    }

    private Document getXmlDocument(String fileName) {
        try {
            if (hotReadSqlXml) {
                Resource resource = new ClassPathResource("sql/" + fileName + ".xml");
                return new SAXReader().read(resource.getFile());
            } else {
                if (xmlDocuments.containsKey(fileName)) {
                    return xmlDocuments.get(fileName);
                } else {
                    Resource resource = new ClassPathResource("sql/" + fileName + ".xml");
                    return xmlDocuments.put(fileName, new SAXReader().read(resource.getFile()));
                }
            }
        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Object xmlToQuery(String fileName, String sqlElement, HttpServletRequest request, Function<Query, Object> function) {
        Element rootElement = getXmlDocument(fileName).getRootElement();
        Element element = rootElement.element(sqlElement);
        String sql = element.getTextTrim();
        SqlHandler sqlHandler = getSqlHandler(rootElement);
        Map<String, Object> param = null;
        if (null != sqlHandler) {
            param = new HashMap<>();
            sql = sqlHandler.handler(element, sql, param);
        }
        Query query = entityManager.createNativeQuery(sql);
        {
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                if (sql.contains(":" + parameterName)) {
                    String val = request.getParameter(parameterName);
                    if (StringUtils.isBlank(val)) {
                        val = "";
                    }
                    query.setParameter(parameterName, val);
                }
            }
        }
        Object result = function.apply(query);
        if (null != sqlHandler) {
            return sqlHandler.handlerResult(element, result, param);
        } else {
            return result;
        }
    }


    private SqlHandler getSqlHandler(Element element) {
        Attribute handlerAttr = element.attribute(RootElementAttribute.HANDLER);
        if (null != handlerAttr) {
            try {
                return (SqlHandler) SpringUtil.getBean(Class.forName(handlerAttr.getValue()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
