package xyz.erupt.atlas.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.atlas.constant.AtlasConstant;
import xyz.erupt.atlas.service.AtlasUsageService;
import xyz.erupt.atlas.service.EruptAtlasService;
import xyz.erupt.atlas.vo.AtlasView;
import xyz.erupt.atlas.vo.PowerRow;
import xyz.erupt.atlas.vo.Usage;
import xyz.erupt.atlas.vo.ModelDetail;
import xyz.erupt.upms.annotation.EruptMenuAuth;

import java.util.List;

/**
 * @author YuePeng
 */
@RestController
@RequestMapping(AtlasConstant.REST_ATLAS)
public class EruptAtlasController {

    @Resource
    private EruptAtlasService eruptAtlasService;

    @Resource
    private AtlasUsageService atlasUsageService;

    @GetMapping("/view")
    @EruptMenuAuth(AtlasConstant.MENU_ATLAS)
    public AtlasView view() {
        return eruptAtlasService.build();
    }

    // Fetched only when its tab is opened: it does not belong in the graph payload
    @GetMapping("/power")
    @EruptMenuAuth(AtlasConstant.MENU_ATLAS)
    public List<PowerRow> power() {
        return eruptAtlasService.power();
    }

    // What every module reports it has bound a model to; fetched once, grouped by the page
    @GetMapping("/usage")
    @EruptMenuAuth(AtlasConstant.MENU_ATLAS)
    public List<Usage> usage() {
        return atlasUsageService.usages();
    }

    // Fetched when a model is opened: field lists for the whole registry would dwarf the graph
    @GetMapping("/detail/{erupt}")
    @EruptMenuAuth(AtlasConstant.MENU_ATLAS)
    public ModelDetail detail(@PathVariable("erupt") String erupt) {
        return eruptAtlasService.detail(erupt);
    }

}
