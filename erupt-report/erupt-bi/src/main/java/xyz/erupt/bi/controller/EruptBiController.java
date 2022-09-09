package xyz.erupt.bi.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.bi.config.EruptBiProp;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.bi.constant.ColumnType;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.bi.model.*;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.*;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.excel.service.EruptExcelService;
import xyz.erupt.excel.util.ExcelUtil;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Slf4j
@RestController
@RequestMapping(BiConst.BATH_PATH)
public class EruptBiController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private BiService biService;

    @Resource
    private EruptProp eruptProp;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private EruptBiProp eruptBiProp;

    @RequestMapping("/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiVo getBuilder(@PathVariable("code") String code, HttpServletResponse response) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {{
            this.put("code", code);
        }});
        if (null == bi) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        BiVo biVo = new BiVo();
        biVo.setPageType(Optional.ofNullable(bi.getPageType()).orElse(BiConst.PAGE_END));
        if (StringUtils.isBlank(bi.getSqlStatement())) {
            biVo.setTable(false);
            biVo.setExport(false);
        } else {
            biVo.setTable(true);
            biVo.setExport(bi.getExport());
        }
        int maxSort = 9999;
        List<BiChartVo> biChartVos = new ArrayList<>();
        for (BiChart chart : bi.getBiCharts()) {
            BiChartVo biChartVo = new BiChartVo();
            biChartVo.setChartOption(chart.getChartOption());
            biChartVo.setId(chart.getId());
            biChartVo.setCode(chart.getCode());
            biChartVo.setGrid(chart.getGrid());
            biChartVo.setHeight(chart.getHeight());
            biChartVo.setName(chart.getName());
            biChartVo.setType(chart.getType());
            biChartVo.setSort((chart.getSort() == null) ? ++maxSort : chart.getSort());
            biChartVos.add(biChartVo);
            biVo.setCharts(biChartVos);
        }
        List<BiDimensionVo> biDimensionVos = new ArrayList<>();
        for (BiDimension dimension : bi.getBiDimension()) {
            BiDimensionVo biDimensionVo = new BiDimensionVo();
            biDimensionVo.setId(dimension.getId());
            biDimensionVo.setCode(dimension.getCode());
            biDimensionVo.setNotNull(dimension.getNotNull());
            biDimensionVo.setTitle(dimension.getTitle());
            biDimensionVo.setType(dimension.getType());
            dimension.setSort((dimension.getSort() == null) ? ++maxSort : dimension.getSort());
            if (StringUtils.isNotBlank(dimension.getDefaultValue())) {
                try {
                    biDimensionVo.setDefaultValue(biService.evalScript(dimension.getDefaultValue()));
                } catch (ScriptException e) {
                    log.error("{}.{} -> {}", bi.getName(), dimension.getCode(), e.getMessage());
                    throw new RuntimeException(e);
                }
            }
            biDimensionVos.add(biDimensionVo);
        }
        if (null != bi.getRefreshTime() && bi.getRefreshTime() > 0) {
            biVo.setRefreshTime(bi.getRefreshTime());
        }
        biVo.setId(bi.getId());
        biVo.setCode(bi.getCode());
        biVo.setCharts(biChartVos.stream().sorted(Comparator.comparing(BiChartVo::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList()));
        biVo.setDimensions(biDimensionVos.stream().sorted(Comparator.comparing(BiDimensionVo::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList()));
        biVo.setPageSize(eruptBiProp.getPageSize());
        biVo.setPageSizeOptions(eruptBiProp.getPageSizeOptions());
        return biVo;
    }

    @PostMapping("/data/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 2)
    public BiData queryData(@RequestParam("index") int pageIndex,
                            @RequestParam("size") int pageSize,
                            @RequestParam(value = "sort", required = false) String sortField,
                            @RequestParam(value = "direction", required = false) Boolean direction,
                            @RequestBody Map<String, Object> query, @PathVariable String code) {
        if (pageSize >= eruptBiProp.getSingleMaxResultNum()) {
            throw new EruptWebApiRuntimeException("exceed single maximum 'pageSize'");
        }
        Bi bi = eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {{
            this.put("code", code);
        }});
        this.validateQuery(bi, query);
        biService.verifyBiMenuPermissions(bi, code);
        if (null != sortField) {
            sortField += " " + (direction ? "asc" : "desc");
        }
        return biService.queryBiData(bi, pageIndex, pageSize, sortField, query, false);
    }

    @PostMapping("/drill/data/{code}/{drillCode}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 3)
    public BiData drillData(@PathVariable String code,
                            @PathVariable Integer drillCode,
                            @RequestParam int pageIndex,
                            @RequestParam int pageSize,
                            @RequestBody Map<String, Object> query) {
        if (pageSize >= eruptBiProp.getSingleMaxResultNum())
            throw new EruptWebApiRuntimeException("exceed single maximum 'pageSize'");
        Bi bi = eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {{
            this.put("code", code);
        }});
        for (BiColumn biColumn : bi.getBiColumns()) {
            if (drillCode == biColumn.getName().hashCode()) {
                BiData biData = new BiData();
                biData.setTotal(biService.getTotal(biColumn.getDrillExpress(), bi.getDataSource(), query));
                if (biData.getTotal() > 0) {
                    biData.setList(biService.startQuery(biColumn.getName() + "-drill",
                            biService.getLimitSql(bi.getDataSource(), biColumn.getDrillExpress(), null, pageIndex, pageSize),
                            1, null, bi.getDataSource(), query));
                    List<BiColumnVo> biColumnVos = new LinkedList<>();
                    biData.getList().get(0).keySet().forEach(key ->
                            biColumnVos.add(new BiColumnVo(key, null, false, true, ColumnType.STRING)));
                    biData.setColumns(biColumnVos);
                }
                return biData;
            }
        }
        throw new EruptWebApiRuntimeException(drillCode + " column not found");
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/reference/{id}")
    public List<Reference> refQuery(@PathVariable("id") Long dimId, @PathVariable String code,
                                    @RequestBody Map<String, Object> query) {
        BiDimension dimension = entityManager.find(BiDimension.class, dimId);
        biService.verifyBiMenuPermissions(dimension.getBi(), code);
        BiDimensionReference reference = dimension.getBiDimensionReference();
        if (null == reference) {
            throw new RuntimeException("未绑定查询维度");
        }
        List<Map<String, Object>> list = biService.startQuery(reference.getName(), reference.getRefSql(), null, reference.getClassHandler(), reference.getDataSource(), query);
        List<Reference> references = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (map.keySet().size() == 1) {
                Object obj = map.values().iterator().next();
                references.add(new Reference(obj, obj));
            } else if (map.keySet().size() >= 3) {
                Iterator<?> iterator = map.values().iterator();
                references.add(new Reference(iterator.next(), iterator.next(), iterator.next()));
            } else {
                Iterator<?> iterator = map.values().iterator();
                references.add(new Reference(iterator.next(), iterator.next()));
            }
        }
        return references;
    }

    /**
     * @param chartId 图表ID
     * @param code    报表编码
     * @param query   查询条件
     */
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/chart/{id}")
    public List<Map<String, Object>> biChart(@PathVariable("id") Long chartId,
                                             @PathVariable("code") String code,
                                             @RequestBody(required = false) Map<String, Object> query) {
        BiChart chart = entityManager.find(BiChart.class, chartId);
        biService.verifyBiMenuPermissions(chart.getBi(), code);
        this.validateQuery(chart.getBi(), query);
        return biService.startQuery(chart.getName(), chart.getSqlStatement(), chart.getCacheTime(), chart.getClassHandler(), chart.getDataSource(), query);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/excel/{id}")
    public void exportExcel(@PathVariable("id") Long id,
                            @PathVariable("code") String code,
                            @RequestBody Map<String, Object> query,
                            HttpServletRequest request,
                            HttpServletResponse response) throws ClassNotFoundException, IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        Bi bi = biService.findBi(id);
        this.validateQuery(bi, query);
        biService.verifyBiMenuPermissions(bi, code);
        Erupts.requireTrue(bi.getExport(), bi.getName() + "禁止导出！");
        BiData biData = biService.queryBiData(bi, 1, Integer.MAX_VALUE, null, query, true);
        try (Workbook wb = new SXSSFWorkbook()) {
            //基本信息
            Sheet sheet = wb.createSheet(bi.getName());
            sheet.createFreezePane(0, 1, 1, 1);
            Row headRow = sheet.createRow(0);
            CellStyle headStyle = ExcelUtil.beautifyExcelStyle(wb);
            Font headFont = wb.createFont();
            headFont.setColor(IndexedColors.WHITE.index);
            headStyle.setFont(headFont);
            headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            int order = 0;
            for (int i = 0; i < biData.getColumns().size(); i++) {
                BiColumnVo biColumn = biData.getColumns().get(i);
                if (biColumn.getDisplay()) {
                    Cell cell = headRow.createCell(order);
                    cell.setCellStyle(headStyle);
                    sheet.setColumnWidth(order, (biColumn.getName().length() + 10) * 256);
                    cell.setCellValue(biColumn.getName());
                    order++;
                }
            }
            CellStyle style = ExcelUtil.beautifyExcelStyle(wb);
            Font font = wb.createFont();
            font.setColor(IndexedColors.BLACK1.index);
            style.setFont(font);
            for (int i = 0; i < biData.getList().size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> map = biData.getList().get(i);
                order = 0;
                for (int j = 0; j < biData.getColumns().size(); j++) {
                    if (biData.getColumns().get(j).getDisplay()) {
                        Object value = map.get(biData.getColumns().get(j).getName());
                        if (null != value) {
                            Cell cell = row.createCell(order);
                            cell.setCellStyle(style);
                            cell.setCellValue(value.toString());
                        }
                        order++;
                    }
                }
            }
            if (null != bi.getClassHandler()) {
                BiClassHandler biClassHandler = bi.getClassHandler();
                EruptBiHandler biHandler = EruptSpringUtil.getBeanByPath(biClassHandler.getHandlerPath(), EruptBiHandler.class);
                biHandler.exportHandler(biClassHandler.getParam(), query, wb);
            }
            wb.write(EruptUtil.downLoadFile(request, response, bi.getName() + "_" + DateUtil.getSimpleFormatDate(new Date()) + EruptExcelService.XLSX_FORMAT));
        }
    }

    //校验查询参数
    private void validateQuery(Bi bi, Map<String, Object> query) {
        for (BiDimension dimension : bi.getBiDimension()) {
            if (dimension.getNotNull()) {
                if (null == query || null == query.get(dimension.getCode())) {
                    throw new EruptApiErrorTip(EruptApiModel.Status.WARNING, dimension.getTitle() + "必填！",
                            EruptApiModel.PromptWay.MESSAGE);
                }
            }
        }
    }

}
