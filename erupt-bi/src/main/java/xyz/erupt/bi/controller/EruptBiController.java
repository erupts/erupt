package xyz.erupt.bi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.bi.model.Bi;
import xyz.erupt.bi.model.BiDimension;
import xyz.erupt.bi.service.BiService;
import xyz.erupt.bi.view.BiColumn;
import xyz.erupt.bi.view.BiData;
import xyz.erupt.bi.view.BiModel;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.tool.EruptDao;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @RequestMapping("/build/{code}")
//    @EruptRouter(verifyErupt = false, authIndex = 2)
    public BiModel getBuilder(@PathVariable("code") String code) {
        Bi bi = eruptDao.queryEntity(Bi.class, "code = '" + code + "'");
        BiModel biModel = new BiModel();
        biModel.setExport(bi.getExport());
        biModel.setCharts(bi.getBiCharts());
        for (BiDimension dimension : bi.getBiDimension()) {
            dimension.setRefSql(null);
        }
        biModel.setDimensions(bi.getBiDimension());
        return biModel;
    }

    @PostMapping("/data/{code}")
    @ResponseBody
    public BiData getData(@PathVariable("code") String code, @RequestBody Map<String, Object> query) {
        List<Map<String, Object>> list = biService.processBiSql(code, query);
        BiData biData = new BiData();
        biData.setList(list);
        if (null != list && list.size() > 0) {
            Set<BiColumn> biColumns = new HashSet<>();
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

    @RequestMapping("{code}/ref/{dim_code}")
    public Object refQuery(@PathVariable("code") String code
            , @PathVariable("dim_code") String dimCode) {
        eruptDao.queryEntity(BiDimension.class, "code = " + code);
        return null;
    }
}
