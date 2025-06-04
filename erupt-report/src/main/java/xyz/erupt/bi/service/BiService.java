package xyz.erupt.bi.service;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.bi.annotation.ChartColumn;
import xyz.erupt.bi.config.EruptBiProp;
import xyz.erupt.bi.constant.*;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.bi.handler.NamedRowMapper;
import xyz.erupt.bi.model.*;
import xyz.erupt.bi.view.BiColumnVo;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.core.cache.EruptCache;
import xyz.erupt.core.cache.EruptCacheLRU;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptMutualConst;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.excel.service.EruptExcelService;
import xyz.erupt.excel.util.ExcelUtil;
import xyz.erupt.upms.constant.EruptReqHeaderConst;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.Bindings;
import javax.script.SimpleBindings;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Resource
    private BiDataSourceService dataSourceService;

    @Resource
    private EruptBiProp eruptBiProp;

    @Resource
    private ScriptService scriptService;

    private final Gson gson = GsonFactory.getGson();

    public String getLimitSql(BiDataSource biDataSource, String sql, String sort, Integer index, Integer size) {
        if (null == biDataSource) {
            return DBTypeEnum.MySQL.processDialect(sql, sort, index, size);
        }
        if (DBTypeEnum.Other.name().equals(biDataSource.getType())) {
            return DBTypeEnum.Other.processDialect(biDataSource.getLimitSql(), sql, sort, index, size);
        }
        return Stream.of(DBTypeEnum.values())
                .filter(it -> it.name().equals(biDataSource.getType()))
                .findFirst().orElse(DBTypeEnum.MySQL).processDialect(sql, sort, index, size);
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

    private final EruptCache<List<Map<String, Object>>> eruptCache = new EruptCacheLRU<>(500);

    public BiData queryBiData(Bi bi, int pageIndex, int pageSize, String sort,
                              Map<String, Object> query, boolean export) {
        Erupts.requireTrue(StringUtils.isNotBlank(bi.getSqlStatement()), "express not found");
        query.put(ScriptPlaceholderConst.EXPORT_PLACEHOLDER, export);
        query.put(ScriptPlaceholderConst.PAGE_SIZE_PLACEHOLDER, pageSize);
        query.put(ScriptPlaceholderConst.PAGE_INDEX_PLACEHOLDER, pageIndex);
        this.putCommonContextParam(query);
        BiData biData = new BiData();
        if (BiConst.PAGE_NONE.equals(bi.getPageType()) || BiConst.PAGE_FRONT.equals(bi.getPageType()) || export) {
            String sql = StringUtils.isBlank(sort) ? bi.getSqlStatement() : String.format("select * from (%s) _t order by %s", bi.getSqlStatement(), sort);
            biData.setList(this.startQuery(bi.getName(), sql, bi.getCacheTime(), bi.getClassHandler(), bi.getDataSource(), query));
            biData.setTotal((long) biData.getList().size());
        } else {
            biData.setTotal(this.getTotal(bi, query));
            if (biData.getTotal() > 0) {
                biData.setList(this.startQuery(bi.getName(), this.getLimitSql(bi.getDataSource(), bi.getSqlStatement(), sort, pageIndex, pageSize),
                        bi.getCacheTime(), bi.getClassHandler(), bi.getDataSource(), query)
                );
            } else {
                biData.setList(new ArrayList<>(0));
            }
        }
        if (null != biData.getList() && !biData.getList().isEmpty()) {
            List<BiColumnVo> biColumnVos = new LinkedList<>();
            Map<String, Object> map = biData.getList().get(0);
            Map<String, BiColumn> columnMap = new HashMap<>();
            Optional.ofNullable(bi.getBiColumns()).ifPresent(it ->
                    bi.getBiColumns().forEach(column -> columnMap.put(column.getName(), column)));
            map.keySet().forEach(key -> {
                if (columnMap.containsKey(key)) {
                    BiColumn biColumn = columnMap.get(key);
                    biColumnVos.add(new BiColumnVo(key, biColumn.getWidth(), biColumn.getSortable(), biColumn.getDisplay(), biColumn.getType(), biColumn.getRemark()));
                } else {
                    biColumnVos.add(new BiColumnVo(key, null, false, true, ColumnType.STRING.getCode(), null));
                }
            });
            biData.setColumns(biColumnVos);
        }
        return biData;
    }

    /**
     * @param key          查询标识
     * @param express      查询表达式
     * @param cacheTime    缓存时间（秒）
     * @param classHandler 处理类
     * @param biDataSource 数据源
     * @param query        查询参数对象
     * @return 查询结果
     */
    @SneakyThrows
    public List<Map<String, Object>> startQuery(String key, String express, Integer cacheTime, BiClassHandler classHandler,
                                                BiDataSource biDataSource, Map<String, Object> query, RowMapper<Map<String, Object>> rowMapper) {
        EruptBiHandler biHandler = null;
        this.putCommonContextParam(query);
        express = processPlaceHolder(express, query);
        if (null != classHandler) {
            biHandler = EruptSpringUtil.getBeanByPath(classHandler.getHandlerPath(), EruptBiHandler.class);
            express = biHandler.exprHandler(classHandler.getParam(), query, express);
        }
        String finalExpress = String.format("/* erupt bi query → %s */ ", key) + express;
        EruptBiHandler finalBiHandler = biHandler;
        if (eruptBiProp.getQueryLog()) {
            log.info(finalExpress);
        }
        Supplier<List<Map<String, Object>>> supplier = () -> {
            NamedParameterJdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(biDataSource);
            List<Map<String, Object>> list = jdbcTemplate.query(finalExpress, query, rowMapper);
            Optional.ofNullable(finalBiHandler).ifPresent(it -> it.resultHandler(classHandler.getParam(), query, list));
            return list;
        };
        if (eruptBiProp.getEnableCache()) {
            query.remove(ScriptPlaceholderConst.REQUEST_PLACEHOLDER);
            query.remove(ScriptPlaceholderConst.RESPONSE_PLACEHOLDER);
            return null == cacheTime || 0 == cacheTime ? supplier.get() : eruptCache.getAndSet(express + gson.toJson(query), cacheTime * 1000, supplier);
        } else {
            return supplier.get();
        }
    }

    public List<Map<String, Object>> startQuery(String key, String express, Integer cacheTime, BiClassHandler classHandler, BiDataSource biDataSource, Map<String, Object> query) {
        return this.startQuery(key, express, cacheTime, classHandler, biDataSource, query, new NamedRowMapper());
    }

    @SneakyThrows
    public List<Map<String, Object>> chartQuery(BiChart chart, Map<String, Object> query) {
        RowMapper<Map<String, Object>> rowMapper;
        if (null != chart.getType()) {
            ChartColumn chartColumn = ChartTypeEnum.class.getDeclaredField(chart.getType().name()).getAnnotation(ChartColumn.class);
            if (null != chartColumn) {
                rowMapper = (rs, i) -> {
                    Map<String, Object> map = new HashMap<>(chartColumn.value().length);
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int index = 1; index <= columnCount && index <= chartColumn.value().length; index++) {
                        map.put(chartColumn.value()[index - 1], rs.getObject(index));
                    }
                    return map;
                };
            } else {
                rowMapper = new NamedRowMapper();
            }
        } else {
            rowMapper = new NamedRowMapper();
        }
        return this.startQuery(chart.getName(), chart.getSqlStatement(), chart.getCacheTime(), chart.getClassHandler(), chart.getDataSource(), query, rowMapper);
    }

    private static final String TOTAL_KEY = "count";

    @SneakyThrows
    public Long getTotal(Bi bi, Map<String, Object> query) {
        String express;
        if (StringUtils.isNotBlank(bi.getCountStatement())) {
            express = processPlaceHolder(bi.getCountStatement(), query);
        } else {
            express = processPlaceHolder(bi.getSqlStatement(), query);
        }
        BiClassHandler biClassHandler = bi.getClassHandler();
        if (null != biClassHandler) {
            express = EruptSpringUtil.getBeanByPath(biClassHandler.getHandlerPath(), EruptBiHandler.class)
                    .exprHandler(biClassHandler.getParam(), query, express);
        }
        if (StringUtils.isNotBlank(bi.getCountStatement())) {
            return dataSourceService.getJdbcTemplate(bi.getDataSource()).queryForObject(express, query, Long.class);
        } else {
            return Long.valueOf(dataSourceService.getJdbcTemplate(bi.getDataSource())
                    .queryForMap("select count(1) " + TOTAL_KEY + " from (\n " + express + " \n) cnt", query)
                    .get(TOTAL_KEY).toString());
        }
    }

    public Long getTotal(String express, BiDataSource biDataSource, Map<String, Object> query) {
        return Long.valueOf(dataSourceService.getJdbcTemplate(biDataSource)
                .queryForMap("select count(1) " + TOTAL_KEY + " from (\n " + express + " \n) cnt", query)
                .get(TOTAL_KEY).toString());
    }

    public final Pattern EXPRESS_PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=\\})");

    //插入通用上下文条件
    private void putCommonContextParam(Map<String, Object> param) {
        param.put(ScriptPlaceholderConst.REQUEST_PLACEHOLDER, request);
        param.put(ScriptPlaceholderConst.RESPONSE_PLACEHOLDER, response);
        param.put(ScriptPlaceholderConst.USER_ID_PLACEHOLDER, eruptUserService.getCurrentUid());
        param.putAll(MetaContext.getVars());
    }

    @SneakyThrows
    private String processPlaceHolder(String express, Map<String, Object> param) {
        Bindings bindings = new SimpleBindings();
        Optional.ofNullable(param).ifPresent(it -> it.forEach((key, value) -> bindings.put(key, it.get(key))));
        Matcher m = EXPRESS_PATTERN.matcher(express);
        while (m.find()) {
            String exp = m.group();
            Object result = scriptService.eval(exp, bindings);
            result = Optional.ofNullable(result).orElse("");
            express = express.replace("${" + exp + "}", result.toString());
        }
        return express;
    }

    @SneakyThrows
    public void exportExcel(String name,
                            Map<String, Object> query,
                            List<KV<String, String>> header,
                            List<Map<String, Object>> data,
                            BiClassHandler biClassHandler,
                            HttpServletRequest request,
                            HttpServletResponse response
    ) {
        try (Workbook wb = new SXSSFWorkbook()) {
            //基本信息
            Sheet sheet = wb.createSheet(name);
            sheet.createFreezePane(0, 1, 1, 1);
            Row headRow = sheet.createRow(0);
            CellStyle headStyle = ExcelUtil.beautifyExcelStyle(wb);
            Font headFont = wb.createFont();
            headFont.setColor(IndexedColors.WHITE.index);
            headStyle.setFont(headFont);
            headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < header.size(); i++) {
                KV<String, String> column = header.get(i);
                Cell cell = headRow.createCell(i);
                cell.setCellStyle(headStyle);
                sheet.setColumnWidth(i, (column.getValue().length() + 10) * 256);
                cell.setCellValue(column.getValue());
            }
            CellStyle style = ExcelUtil.beautifyExcelStyle(wb);
            Font font = wb.createFont();
            font.setColor(IndexedColors.BLACK1.index);
            style.setFont(font);
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> map = data.get(i);
                for (int j = 0; j < header.size(); j++) {
                    Object value = map.get(header.get(j).getKey());
                    if (null != value) {
                        Cell cell = row.createCell(j);
                        cell.setCellStyle(style);
                        cell.setCellValue(value.toString());
                    }
                }
            }
            if (null != biClassHandler) {
                EruptBiHandler biHandler = EruptSpringUtil.getBeanByPath(biClassHandler.getHandlerPath(), EruptBiHandler.class);
                biHandler.exportHandler(biClassHandler.getParam(), query, wb);
            }
            wb.write(ExcelUtil.downLoadFile(request, response, name + "_" + DateUtil.getFormatDate(new Date(), "yyyy-MM-dd_HH-mm-ss") + EruptExcelService.XLSX_FORMAT));
        }
    }

}
