package xyz.erupt.bi.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.constant.DBTypeEnum;
import xyz.erupt.bi.constant.ScriptPlaceholderConst;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiClassHandler;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2020-02-12
 */
@Service
@Slf4j
public class BiService {

    private static final String TOTAL_KEY = "count";

    @Resource
    private EruptUserService eruptUserService;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Resource
    private BiDataSourceService dataSourceService;

    private String getLimitSql(BiDataSource biDataSource) {
        if (null == biDataSource) {
            return DBTypeEnum.GENERAL_LIMIT;
        }
        if (StringUtils.isNotBlank(biDataSource.getLimitSql())) {
            return biDataSource.getLimitSql();
        }
        return Stream.of(DBTypeEnum.values())
                .filter(it -> it.name().equals(biDataSource.getType()))
                .findFirst().orElse(DBTypeEnum.MySQL).getLimitSql();
    }

    public Bi findBi(Long id) {
        return entityManager.find(Bi.class, id);
    }

    public BiData queryBiData(Bi bi, int pageIndex, int pageSize,
                              Map<String, Object> query, boolean export) {
        Erupts.requireTrue(StringUtils.isNotBlank(bi.getSqlStatement()), "express not found");
        query.put(ScriptPlaceholderConst.EXPORT_PLACEHOLDER, export);
        query.put(ScriptPlaceholderConst.USER_ID_PLACEHOLDER, eruptUserService.getCurrentUid());
        query.put(ScriptPlaceholderConst.REQUEST_PLACEHOLDER, request);
        query.put(ScriptPlaceholderConst.RESPONSE_PLACEHOLDER, response);
        BiData biData = new BiData();
        if (!export) {
            biData.setTotal(this.getTotal(bi, query));
        }
        if (null == biData.getTotal() || biData.getTotal() > 0) {
            String sql = this.getLimitSql(bi.getDataSource())
                    .replace(DBTypeEnum.$SQL, bi.getSqlStatement())
                    .replace(DBTypeEnum.$SIZE, String.valueOf(pageSize))
                    .replace(DBTypeEnum.$SKIP, String.valueOf((pageIndex - 1) * pageSize));
            List<Map<String, Object>> list = startQuery(sql, bi.getClassHandler(), bi.getDataSource(), query);
            if (null != list && list.size() > 0) {
                List<BiColumn> biColumns = new LinkedList<>();
                Map<String, Object> map = list.get(0);
                map.keySet().forEach(key -> biColumns.add(new BiColumn(key)));
                biData.setColumns(biColumns);
            }
            biData.setList(list);
        } else {
            biData.setList(new ArrayList<>(0));
        }
        return biData;
    }

    private static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

    @SneakyThrows
    public List<Map<String, Object>> startQuery(String express, BiClassHandler classHandler, BiDataSource biDataSource, Map<String, Object> query) {
        EruptBiHandler biHandler = null;
        express = processPlaceHolder(express, query);
        if (null != classHandler) {
            biHandler = EruptSpringUtil.getBeanByPath(classHandler.getHandlerPath(), EruptBiHandler.class);
            express = biHandler.exprHandler(classHandler.getParam(), query, express);
        }
        NamedParameterJdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(biDataSource);
        List<Map<String, Object>> list = jdbcQuery(jdbcTemplate, express, query);
        Optional.ofNullable(biHandler).ifPresent(it -> it.resultHandler(classHandler.getParam(), query, list));
        return list;
    }

    private static final Pattern EXPRESS_PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=\\})");

    public static Object evalScript(String script, Bindings bindings) throws ScriptException {
        return null == bindings ? scriptEngine.eval(script) : scriptEngine.eval(script, bindings);
    }

    public static Object evalScript(String script) throws ScriptException {
        return evalScript(script, null);
    }

    @SneakyThrows
    private Long getTotal(Bi bi, Map<String, Object> query) {
        String express = processPlaceHolder(bi.getSqlStatement(), query);
        BiClassHandler biClassHandler = bi.getClassHandler();
        if (null != biClassHandler) {
            express = EruptSpringUtil.getBeanByPath(biClassHandler.getHandlerPath(), EruptBiHandler.class)
                    .exprHandler(biClassHandler.getParam(), query, express);
        }
        return Long.valueOf(dataSourceService.getJdbcTemplate(bi.getDataSource())
                .queryForMap(String.format("select count(*) %s from (%s) count_", TOTAL_KEY, express), query)
                .get(TOTAL_KEY).toString());
    }

    private List<Map<String, Object>> jdbcQuery(NamedParameterJdbcTemplate jdbcTemplate, String express, Map<String, Object> query) {
        log.info(express);
        return jdbcTemplate.query(express, query, (rs, i) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int index = 1; index <= columnCount; index++) {
                map.put(metaData.getColumnLabel(index), rs.getObject(index));
            }
            return map;
        });
    }

    @SneakyThrows
    private String processPlaceHolder(String express, Map<String, Object> param) {
        Bindings bindings = new SimpleBindings();
        Optional.ofNullable(param).ifPresent(it -> it.forEach((key, value) -> bindings.put(key, it.get(key))));
        Matcher m = EXPRESS_PATTERN.matcher(express);
        while (m.find()) {
            String exp = m.group();
            Object result = scriptEngine.eval(BiDataInitService.defineFunctions + "\n" + exp, bindings);
            result = Optional.ofNullable(result).orElse("");
            express = express.replace("${" + exp + "}", result.toString());
        }
        return express;
    }
}
