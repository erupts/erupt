package xyz.erupt.bi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiChart;
import xyz.erupt.bi.model.BiDimension;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.bi.view.BiModel;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.tool.EruptDao;

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

    private static final Integer MAX_SORT = 9999;

    @RequestMapping("/{code}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiModel getBuilder(@PathVariable("code") String code) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = '" + code + "'");
        BiModel biModel = new BiModel();
        biModel.setExport(bi.getExport());
        for (BiChart chart : bi.getBiCharts()) {
            chart.setSqlStatement(null);
            if (chart.getSort() == null) {
                chart.setSort(MAX_SORT);
            }
        }
        for (BiDimension dimension : bi.getBiDimension()) {
            dimension.setRefSql(null);
            if (dimension.getSort() == null) {
                dimension.setSort(MAX_SORT);
            }
        }
        biModel.setCharts(bi.getBiCharts().stream().sorted(Comparator.comparing(BiChart::getSort)).collect(Collectors.toList()));
        biModel.setDimensions(bi.getBiDimension().stream().sorted(Comparator.comparing(BiDimension::getSort)).collect(Collectors.toList()));
        return biModel;
    }

    @PostMapping("/{code}/data")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    public BiData getData(@PathVariable("code") String code, @RequestBody Map<String, Object> query) {
        List<Map<String, Object>> list = biService.startQuery(code, query);
        BiData biData = new BiData();
        biData.setList(list);
        if (null != list && list.size() > 0) {
            Set<BiColumn> biColumns = new LinkedHashSet<>();
            Map<String, Object> map = list.get(0);
            for (String key : map.keySet()) {
                BiColumn biColumn = new BiColumn();
                biColumn.setName(key);
                biColumns.add(biColumn);
            }
            biData.setColumns(biColumns);
        }
        return biData;
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/ref/{dim}")
    public Object refQuery(@PathVariable("code") String code
            , @PathVariable("dim") String dimension) {
        eruptDao.queryEntity(BiDimension.class, "code = " + code);
        return null;
    }

    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU, authIndex = 1)
    @RequestMapping("{code}/ref/{chart}")
    public Object biChart(@PathVariable("code") String code
            , @PathVariable("chart") String dimCode) {
        eruptDao.queryEntity(BiDimension.class, "code = " + code);
        return null;
    }
}
