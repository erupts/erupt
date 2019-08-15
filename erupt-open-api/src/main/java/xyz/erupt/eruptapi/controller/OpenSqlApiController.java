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
import xyz.erupt.eruptapi.constant.RootElementAttribute;
import xyz.erupt.eruptcommon.util.IpUtil;

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
        Query query = xmlToQuery(fileName, sqlElement, request);
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
        Query query = xmlToQuery(fileName, sqlElement, request);
        return query.executeUpdate() != 0;
    }

    private Query xmlToQuery(String fileName, String sqlElement, HttpServletRequest request) {
        try {
            Resource resource = new ClassPathResource("sql/" + fileName + ".xml");
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(resource.getFile());
            Element rootElement = document.getRootElement();
            if (null != rootElement.attribute(RootElementAttribute.allowIp)) {
                String[] ips = rootElement.attribute(RootElementAttribute.allowIp).getValue().split(",");
                String ipAddr = IpUtil.getIpAddr(request);
                boolean isAllowIp = false;
                for (String ip : ips) {
                    if (ip.equals(ipAddr)) {
                        isAllowIp = true;
                        break;
                    }
                }
                if (!isAllowIp) {
                    throw new RuntimeException("ip error");
                }
            }
            Element element = rootElement.element(sqlElement);
            String sql = element.getTextTrim();
            Query query = entityManager.createNativeQuery(sql);
            {
                Enumeration<String> parameterNames = request.getParameterNames();
                while (parameterNames.hasMoreElements()) {
                    String parameterName = parameterNames.nextElement();
                    if (sql.contains(":" + parameterName)) {
                        query.setParameter(parameterName, request.getParameter(parameterName));
                    }
                }
            }
            return query;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


}
