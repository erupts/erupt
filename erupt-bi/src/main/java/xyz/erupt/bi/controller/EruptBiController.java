package xyz.erupt.bi.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiDimension;
import xyz.erupt.bi.model.BiDimensionReference;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.bi.view.BiModel;
import xyz.erupt.bi.view.Reference;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.service.DataFileService;
import xyz.erupt.core.util.HttpUtil;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.tool.EruptDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyuepeng
 * @date 2019-08-26.
 */
@RestController
@RequestMapping(RestPath.ERUPT_API + "/bi")
public class EruptBiController {

    @Autowired
    private EruptDao eruptDao;

    @Autowired
    private BiService biService;

    @RequestMapping("/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiModel getBuilder(@PathVariable("code") String code, HttpServletResponse response) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = :code", new HashMap<String, Object>(1) {
            {
                this.put("code", code);
            }
        });
        if (null == bi) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        BiModel biModel = new BiModel();
        if (StringUtils.isBlank(bi.getSqlStatement())) {
            biModel.setTable(false);
        } else {
            biModel.setTable(true);
        }
        biModel.setExport(bi.getExport());
        int maxSort = 9999;
        for (BiChart chart : bi.getBiCharts()) {
            chart.setSqlStatement(null);
            chart.setBi(null);
            if (chart.getSort() == null) {
                chart.setSort(++maxSort);
            }
        }
        for (BiDimension dimension : bi.getBiDimension()) {
            dimension.setBiDimensionReference(null);
            if (dimension.getSort() == null) {
                dimension.setSort(++maxSort);
            }
        }
        biModel.setCode(bi.getCode());
        biModel.setCharts(bi.getBiCharts().stream().sorted(Comparator.comparing(BiChart::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList()));
        biModel.setDimensions(bi.getBiDimension().stream().sorted(Comparator.comparing(BiDimension::getSort, Comparator.nullsFirst(Integer::compareTo))).collect(Collectors.toList()));
        return biModel;
    }

    @PostMapping("/{code}/data")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiData getData(@PathVariable("code") String code,
//                          @RequestParam(value = "index", required = false) int pageIndex,
//                          @RequestParam(value = "size", required = false) int pageSize,
//                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestBody Map<String, Object> query) {
        return biService.queryBiData(code, query);
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/reference/{dim}")
    public List<Reference> refQuery(@PathVariable("code") String code
            , @PathVariable("dim") String dim) {
        Bi bi = biService.findBi(code);
        for (BiDimension dimension : bi.getBiDimension()) {
            if (dim.equals(dimension.getCode())) {
                BiDimensionReference reference = dimension.getBiDimensionReference();
                List<Map<String, Object>> list = biService.startQuery(reference.getRefSql(), reference.getClassHandler(), reference.getDataSource(), null);
                List<Reference> references = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    if (map.keySet().size() == 1) {
                        Object obj = map.values().iterator().next();
                        references.add(new Reference(obj, obj));
                    } else if (map.keySet().size() > 1) {
                        Iterator iterator = map.values().iterator();
                        references.add(new Reference(iterator.next(), iterator.next()));
                    }
                }
                return references;
            }
        }
        throw new EruptNoLegalPowerException();
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/chart/{chart_code}")
    public List<Map<String, Object>> biChart(@PathVariable("code") String code, @PathVariable("chart_code") String chartCode,
                                             @RequestBody Map<String, Object> query) {
        Bi bi = biService.findBi(code);
        for (BiChart chart : bi.getBiCharts()) {
            if (chartCode.equals(chart.getCode())) {
                return biService.startQuery(chart.getSqlStatement(), chart.getClassHandler(), chart.getDataSource(), query);
            }
        }
        throw new EruptNoLegalPowerException();
    }


    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU
            , verifyMethod = EruptRouter.VerifyMethod.PARAM, authIndex = 1)
    @RequestMapping("{code}/excel")
    public void exportExcel(@PathVariable("code") String code, @RequestBody Map<String, Object> query, HttpServletRequest request, HttpServletResponse response) {
        if (SecurityUtil.csrfInspect(request, response)) {
            throw new RuntimeException("非法请求");
        }
        BiData biData = biService.queryBiData(code, query);
        Workbook wb = new HSSFWorkbook();
        //基本信息
        Sheet sheet = wb.createSheet(biData.getName());
        sheet.createFreezePane(0, 1, 1, 1);
        Row headRow = sheet.createRow(0);
        for (int i = 0; i < biData.getColumns().size(); i++) {
            BiColumn biColumn = biData.getColumns().get(i);
            Cell cell = headRow.createCell(i);
            sheet.setColumnWidth(i, (biColumn.getName().length() + 10) * 256);
            cell.setCellValue(biColumn.getName());
        }
        for (int i = 0; i < biData.getList().size(); i++) {
            Row row = sheet.createRow(i);
            Map<String, Object> map = biData.getList().get(i);
            for (Object value : map.values()) {
                Cell cell = row.createCell(i);
                if (null != value) {
                    cell.setCellValue(value.toString());
                }
            }
        }
        try {
            wb.write(HttpUtil.downLoadFile(response, biData.getName() + DataFileService.XLS_FORMAT));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
