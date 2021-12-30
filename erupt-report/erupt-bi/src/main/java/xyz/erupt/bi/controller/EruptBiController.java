package xyz.erupt.bi.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.sub_erupt.Tpl;
import xyz.erupt.bi.fun.EruptBiHandler;
import xyz.erupt.bi.model.*;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.controller.EruptExceptionHandlerAdvice;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptExcelService;
import xyz.erupt.core.util.*;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.tpl.service.EruptTplService;
import xyz.erupt.upms.constant.EruptReqHeaderConst;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/bi")
@ControllerAdvice("xyz.erupt.bi.controller")
public class EruptBiController {

    @Resource
    private EruptDao eruptDao;

    @Resource
    private BiService biService;

    @Resource
    private EruptProp eruptProp;

    @Resource
    private HttpServletRequest request;

    @PersistenceContext
    private EntityManager entityManager;

    private final Gson gson = GsonFactory.getGson();

    @ResponseBody
    @ExceptionHandler(value = RuntimeException.class)
    public Map<String, Object> biErrorHandler(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        return new EruptExceptionHandlerAdvice().eruptWebApiRuntimeException(ex, request, response);
    }

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
            biChartVo.setPath(chart.getPath());
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
        return biVo;
    }

    @PostMapping("/data/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 2)
    public BiData queryData(@RequestParam("index") int pageIndex,
                            @RequestParam("size") int pageSize,
                            @RequestParam(value = "sort", required = false) String sort,
                            @RequestBody Map<String, Object> query, @PathVariable String code) {
        pageSize = pageSize > 100 ? 100 : pageSize;
        Bi bi = eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {{
            this.put("code", code);
        }});
        this.verifyBiMenuPermissions(bi, code);
        return biService.queryBiData(bi, pageIndex, pageSize, query, false);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/reference/{id}")
    public List<Reference> refQuery(@PathVariable("id") Long dimId, @PathVariable String code,
                                    @RequestBody Map<String, Object> query) {
        BiDimension dimension = entityManager.find(BiDimension.class, dimId);
        this.verifyBiMenuPermissions(dimension.getBi(), code);
        BiDimensionReference reference = dimension.getBiDimensionReference();
        List<Map<String, Object>> list = biService.startQuery(reference.getRefSql(), reference.getClassHandler(), reference.getDataSource(), query);
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

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/chart/{id}")
    public List<Map<String, Object>> biChart(@PathVariable("id") Long chartId,
                                             @RequestBody(required = false) Map<String, Object> query,
                                             @PathVariable String code) {
        BiChart chart = entityManager.find(BiChart.class, chartId);
        this.verifyBiMenuPermissions(chart.getBi(), code);
        return biService.startQuery(chart.getSqlStatement(), chart.getClassHandler(), chart.getDataSource(), query);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("/{code}/excel/{id}")
    public void exportExcel(@PathVariable("id") Long id,
                            @PathVariable("code") String code,
                            @RequestBody Map<String, Object> condition,
                            HttpServletRequest request,
                            HttpServletResponse response) throws ClassNotFoundException, IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        Bi bi = biService.findBi(id);
        this.verifyBiMenuPermissions(bi, code);
        Erupts.requireTrue(bi.getExport(), bi.getName() + "禁止导出！");
        BiData biData = biService.queryBiData(bi, 1, Integer.MAX_VALUE, condition, true);
        Workbook wb = new SXSSFWorkbook();
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
        for (int i = 0; i < biData.getColumns().size(); i++) {
            BiColumn biColumn = biData.getColumns().get(i);
            Cell cell = headRow.createCell(i);
            cell.setCellStyle(headStyle);
            sheet.setColumnWidth(i, (biColumn.getName().length() + 10) * 256);
            cell.setCellValue(biColumn.getName());
        }

        CellStyle style = ExcelUtil.beautifyExcelStyle(wb);
        Font font = wb.createFont();
        font.setColor(IndexedColors.BLACK1.index);
        style.setFont(font);
        for (int i = 0; i < biData.getList().size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> map = biData.getList().get(i);
            for (int j = 0; j < biData.getColumns().size(); j++) {
                Object value = map.get(biData.getColumns().get(j).getName());
                if (null != value) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(style);
                    cell.setCellValue(value.toString());
                }
            }
        }
        if (null != bi.getClassHandler()) {
            BiClassHandler biClassHandler = bi.getClassHandler();
            EruptBiHandler biHandler = EruptSpringUtil.getBeanByPath(biClassHandler.getHandlerPath(), EruptBiHandler.class);
            biHandler.exportHandler(biClassHandler.getParam(), condition, wb);
        }
        wb.write(EruptUtil.downLoadFile(request, response, bi.getName() + EruptExcelService.XLSX_FORMAT));
    }

    @GetMapping(value = "/{code}/custom-chart/{id}", produces = {"text/html;charset=UTF-8"})
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.MENU, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public void customerChart(@PathVariable("id") Long chartId,
                              @RequestParam("condition") String conditionStr,
                              HttpServletResponse response, @PathVariable String code) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> condition = gson.fromJson(URLDecoder.decode(conditionStr, StandardCharsets.UTF_8.name()),
                new TypeToken<Map<String, Object>>() {
                }.getType());
        BiChart biChart = entityManager.find(BiChart.class, chartId);
        this.verifyBiMenuPermissions(biChart.getBi(), code);
        Map<String, Object> map = new HashMap<>();
        map.put("data", biService.startQuery(biChart.getSqlStatement(), biChart.getClassHandler(), biChart.getDataSource(), condition));
        EruptTplService eruptTplService = EruptSpringUtil.getBean(EruptTplService.class);
        eruptTplService.tplRender(Tpl.Engine.FreeMarker, biChart.getPath(), map, response.getWriter());
    }

    //校验请求id是否拥有菜单权限
    private void verifyBiMenuPermissions(Bi bi, String code) {
        String biCode = Optional.ofNullable(request.getHeader(EruptReqHeaderConst.ERUPT_HEADER_KEY))
                .orElse(request.getParameter(EruptReqHeaderConst.URL_ERUPT_PARAM_KEY));
        if (!biCode.equals(bi.getCode()) || !code.equals(bi.getCode())) {
            throw new EruptNoLegalPowerException();
        }
    }

}
