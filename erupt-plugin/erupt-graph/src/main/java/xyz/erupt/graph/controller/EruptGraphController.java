package xyz.erupt.graph.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.graph.constant.GraphConstant;
import xyz.erupt.graph.service.EruptGraphService;
import xyz.erupt.graph.vo.GraphView;
import xyz.erupt.graph.vo.ModelDetail;
import xyz.erupt.upms.annotation.EruptMenuAuth;

/**
 * @author YuePeng
 */
@RestController
@RequestMapping(GraphConstant.REST_GRAPH)
public class EruptGraphController {

    @Resource
    private EruptGraphService eruptGraphService;

    @GetMapping("/view")
    @EruptMenuAuth(GraphConstant.MENU_GRAPH)
    public GraphView view() {
        return eruptGraphService.build();
    }

    // Fetched when a model is opened: field lists for the whole registry would dwarf the graph
    @GetMapping("/detail/{erupt}")
    @EruptMenuAuth(GraphConstant.MENU_GRAPH)
    public ModelDetail detail(@PathVariable("erupt") String erupt) {
        return eruptGraphService.detail(erupt);
    }

}
