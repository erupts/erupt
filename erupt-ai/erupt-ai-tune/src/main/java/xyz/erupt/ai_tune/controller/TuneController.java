package xyz.erupt.ai_tune.controller;

import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai_tune.model.TuneDataset;
import xyz.erupt.ai_tune.model.TuneSample;
import xyz.erupt.ai_tune.service.TuneDatasetService;
import xyz.erupt.ai_tune.service.TuneJobService;
import xyz.erupt.ai_tune.vo.CompareVo;
import xyz.erupt.ai_tune.vo.MonitorVo;
import xyz.erupt.ai_tune.vo.SamplePageVo;
import xyz.erupt.ai_tune.vo.SampleVo;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.view.R;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.dao.EruptLambdaQuery;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Backs the dataset preview, job monitor and compare pages.
 *
 * @author YuePeng
 * date 2026/10/8
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/tune")
public class TuneController {

    private static final int MAX_PAGE_SIZE = 200;

    @Resource
    private EruptDao eruptDao;

    @Resource
    private TuneDatasetService datasetService;

    @Resource
    private TuneJobService tuneJobService;

    @GetMapping("/job/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public R<MonitorVo> monitor(@PathVariable("id") Long id) {
        return R.ok(tuneJobService.monitor(id));
    }

    @PostMapping("/job/{id}/compare")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public R<CompareVo> compare(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        if (null == prompt || prompt.isBlank()) throw new EruptWebApiRuntimeException("Prompt is required");
        return R.ok(tuneJobService.compare(id, body.get("system"), prompt));
    }

    @GetMapping("/dataset/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public R<TuneDataset> dataset(@PathVariable("id") Long id) {
        return R.ok(this.requireDataset(id));
    }

    @GetMapping("/dataset/{id}/samples")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public R<SamplePageVo> samples(@PathVariable("id") Long id,
                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "size", defaultValue = "20") int size,
                                   @RequestParam(value = "invalidOnly", defaultValue = "false") boolean invalidOnly,
                                   @RequestParam(value = "keyword", required = false) String keyword) {
        TuneDataset dataset = this.requireDataset(id);
        page = Math.max(1, page);
        size = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        EruptLambdaQuery<TuneSample> query = eruptDao.lambdaQuery(TuneSample.class).eq(TuneSample::getDataset, dataset)
                .eq(invalidOnly, TuneSample::getValid, false)
                .like(null != keyword && !keyword.isBlank(), TuneSample::getContent, keyword);
        SamplePageVo vo = new SamplePageVo();
        vo.setPage(page);
        vo.setSize(size);
        vo.setTotal(query.count());
        vo.setItems(query.orderByAsc(TuneSample::getSeq).limit(size).offset((page - 1) * size).list()
                .stream().map(SampleVo::of).collect(Collectors.toList()));
        return R.ok(vo);
    }

    @GetMapping("/dataset/{id}/export")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public ResponseEntity<byte[]> export(@PathVariable("id") Long id) {
        TuneDataset dataset = this.requireDataset(id);
        String fileName = dataset.getName().replaceAll("[^\\w.-]+", "_") + ".jsonl";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("application", "jsonl", StandardCharsets.UTF_8))
                .body(datasetService.toJsonl(dataset));
    }

    private TuneDataset requireDataset(Long id) {
        TuneDataset dataset = eruptDao.find(TuneDataset.class, id);
        if (null == dataset) throw new EruptWebApiRuntimeException("Dataset not found: " + id);
        return dataset;
    }

}
