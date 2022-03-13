package xyz.erupt.bi.service;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.constant.DBTypeEnum;
import xyz.erupt.bi.constant.ScriptPlaceholderConst;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.bi.model.*;
import xyz.erupt.bi.view.BiColumnVo;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.toolkit.cache.EruptCache;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.function.Supplier;
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

    @Resource
    private EruptUserService eruptUserService;

    private static String DEFINE_FUNCTIONS = "";

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Resource
    private BiDataSourceService dataSourceService;

    private final Gson gson = GsonFactory.getGson();

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

    //校验请求id是否拥有菜单权限
    public void verifyBiMenuPermissions(Bi bi, String code) {
        String biCode = Optional.ofNullable(request.getHeader(EruptMutualConst.ERUPT))
                .orElse(request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY));
        if (!biCode.equals(bi.getCode()) || !code.equals(bi.getCode())) {
            throw new EruptNoLegalPowerException();
        }
    }

    private final EruptCache<List<Map<String, Object>>> eruptCache = EruptCache.newInstance();

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(BiConst.SCRIPT_ENGINE);

    public BiData queryBiData(Bi bi, int pageIndex, int pageSize, String sort,
                              Map<String, Object> query, boolean export) {
        Erupts.requireTrue(StringUtils.isNotBlank(bi.getSqlStatement()), "express not found");
        query.put(ScriptPlaceholderConst.EXPORT_PLACEHOLDER, export);
        query.put(ScriptPlaceholderConst.PAGE_SIZE_PLACEHOLDER, pageSize);
        query.put(ScriptPlaceholderConst.PAGE_INDEX_PLACEHOLDER, pageIndex);
        this.putCommonContextParam(query);
        BiData biData = new BiData();
        biData.setPageType(Optional.ofNullable(bi.getPageType()).orElse(BiConst.PAGE_END));
        if (BiConst.PAGE_NONE.equals(bi.getPageType()) || BiConst.PAGE_FRONT.equals(bi.getPageType()) || export) {
            biData.setList(startQuery(bi.getSqlStatement(), bi.getCacheTime(), bi.getClassHandler(), bi.getDataSource(), query));
            biData.setTotal((long) biData.getList().size());
        } else {
            biData.setTotal(this.getTotal(bi, query));
            if (biData.getTotal() > 0) {
                biData.setList(startQuery(this.getLimitSql(bi.getDataSource())
                                .replace(DBTypeEnum.$SQL, bi.getSqlStatement())
                                .replace(DBTypeEnum.$SORT, sort + "")
                                .replace(DBTypeEnum.$SIZE, String.valueOf(pageSize))
                                .replace(DBTypeEnum.$SKIP, String.valueOf((pageIndex - 1) * pageSize)),
                        bi.getCacheTime(), bi.getClassHandler(), bi.getDataSource(), query)
                );
            } else {
                biData.setList(new ArrayList<>(0));
            }
        }
        if (null != biData.getList() && biData.getList().size() > 0) {
            List<BiColumnVo> biColumnVos = new LinkedList<>();
            Map<String, Object> map = biData.getList().get(0);
            Map<String, BiColumn> columnMap = new HashMap<>();
            Optional.ofNullable(bi.getBiColumns()).ifPresent(it -> {
                bi.getBiColumns().forEach(column -> columnMap.put(column.getName(), column));
            });
            map.keySet().forEach(key -> {
                if (columnMap.containsKey(key)) {
                    BiColumn biColumn = columnMap.get(key);
                    biColumnVos.add(new BiColumnVo(key, biColumn.getWidth(), biColumn.getOrderBy()));
                } else {
                    biColumnVos.add(new BiColumnVo(key, null, false));
                }
            });
            biData.setColumns(biColumnVos);
        }
        return biData;
    }

    @SneakyThrows
    public List<Map<String, Object>> startQuery(String express, Integer timeout, BiClassHandler classHandler, BiDataSource biDataSource, Map<String, Object> query) {
        EruptBiHandler biHandler = null;
        this.putCommonContextParam(query);
        express = processPlaceHolder(express, query);
        if (null != classHandler) {
            biHandler = EruptSpringUtil.getBeanByPath(classHandler.getHandlerPath(), EruptBiHandler.class);
            express = biHandler.exprHandler(classHandler.getParam(), query, express);
        }
        String finalExpress = express;
        EruptBiHandler finalBiHandler = biHandler;
        Supplier<List<Map<String, Object>>> supplier = () -> {
            NamedParameterJdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(biDataSource);
            List<Map<String, Object>> list = jdbcQuery(jdbcTemplate, finalExpress, query);
            Optional.ofNullable(finalBiHandler).ifPresent(it -> it.resultHandler(classHandler.getParam(), query, list));
            return list;
        };
        return null == timeout ? supplier.get() : eruptCache.getAndSet(express + gson.toJson(query), timeout * 1000, supplier);
    }

    public Object evalScript(String script, Bindings bindings) throws ScriptException {
        return null == bindings ? scriptEngine.eval(script) : scriptEngine.eval(script, bindings);
    }

    public Object evalScript(String script) throws ScriptException {
        return evalScript(script, null);
    }

    private static final String TOTAL_KEY = "count";

    @Resource
    private EruptDao eruptDao;

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
        if (log.isDebugEnabled()) {
            log.debug(express);
        }
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

    public final Pattern EXPRESS_PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=\\})");

    //插入通用上下文条件
    private void putCommonContextParam(Map<String, Object> param) {
        param.put(ScriptPlaceholderConst.REQUEST_PLACEHOLDER, request);
        param.put(ScriptPlaceholderConst.RESPONSE_PLACEHOLDER, response);
        param.put(ScriptPlaceholderConst.USER_ID_PLACEHOLDER, eruptUserService.getCurrentUid());
    }

    @SneakyThrows
    private String processPlaceHolder(String express, Map<String, Object> param) {
        Bindings bindings = new SimpleBindings();
        Optional.ofNullable(param).ifPresent(it -> it.forEach((key, value) -> bindings.put(key, it.get(key))));
        Matcher m = EXPRESS_PATTERN.matcher(express);
        while (m.find()) {
            String exp = m.group();
            Object result = scriptEngine.eval(DEFINE_FUNCTIONS + "\n" + exp, bindings);
            result = Optional.ofNullable(result).orElse("");
            express = express.replace("${" + exp + "}", result.toString());
        }
        return express;
    }

    @PostConstruct
    public void flushFunction() {
        List<Object[]> list = eruptDao.queryObjectList(BiFunction.class, null, null, "jsFunction");
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            sb.append((String) o).append("\n");
        }
        DEFINE_FUNCTIONS = sb.toString();
    }

}
