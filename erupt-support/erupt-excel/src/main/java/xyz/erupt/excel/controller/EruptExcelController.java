package xyz.erupt.excel.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.annotation.EruptRecordOperate;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.controller.EruptModifyController;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.naming.EruptRecordNaming;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptModifyService;
import xyz.erupt.core.service.EruptService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.R;
import xyz.erupt.core.view.TableQuery;
import xyz.erupt.excel.codec.TableCodec;
import xyz.erupt.excel.codec.TableCodecs;
import xyz.erupt.excel.codec.XlsxCodec;
import xyz.erupt.excel.service.EruptExcelService;
import xyz.erupt.excel.util.ExcelUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Import and export in any registered {@link TableCodec} format. The path keeps its historical
 * "excel" segment and Excel stays the default, so a client that sends no format sees no change.
 *
 * @author YuePeng
 * date 10/15/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_EXCEL)
@RequiredArgsConstructor
@Slf4j
public class EruptExcelController {

    private final EruptProp eruptProp;

    private final EruptExcelService excelService;

    private final TableCodecs codecs;

    private final XlsxCodec xlsxCodec;

    private final EruptModifyService eruptModifyService;

    private final EruptService eruptService;

    private final EruptModifyController eruptModifyController;

    // the export formats a client may offer
    @GetMapping("/formats")
    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.LOGIN)
    public List<Map<String, Object>> formats() {
        return codecs.list().stream().map(it -> Map.<String, Object>of("format", it.format(), "name", it.name())).toList();
    }

    @GetMapping(value = "/template/{erupt}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    // The template is always Excel: it carries the validations and hints a text format cannot
    public void template(@PathVariable("erupt") String eruptName,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isImportable);
        TableCodec codec = codecs.get(null);
        codec.write(excelService.template(eruptModel),
                ExcelUtil.downLoadFile(request, response, eruptModel.getErupt().name() + "_template." + codec.format()));
    }

    @PostMapping("/export/{erupt}")
    @EruptRecordOperate(value = "Export", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public void export(@PathVariable("erupt") String eruptName,
                       @RequestBody TableQuery tableQuery,
                       @RequestParam(value = "ids", required = false) List<Object> ids,
                       @RequestParam(value = "format", required = false) String format,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (eruptProp.isCsrfInspect() && SecurityUtil.csrfInspect(request, response)) return;
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isExport);
        TableCodec codec = codecs.get(format);
        tableQuery.setPageIndex(1);
        tableQuery.setPageSize(Page.PAGE_MAX_DATA);
        Page page;
        if (ids != null && !ids.isEmpty()) {
            Condition pkCondition = new Condition(eruptModel.getErupt().primaryKeyCol(), ids, QueryExpression.IN);
            page = eruptService.getEruptData(eruptModel, tableQuery, List.of(pkCondition));
        } else {
            page = eruptService.getEruptData(eruptModel, tableQuery, null);
        }
        String fileName = eruptModel.getErupt().name() + "_" + DateUtil.getFormatDate(new Date(), DateUtil.ISO_8601) + "." + codec.format();
        codec.write(excelService.sheet(eruptModel, page, tableQuery.getCondition()), ExcelUtil.downLoadFile(request, response, fileName));
    }

    @PostMapping("/import/{erupt}")
    @EruptRecordOperate(value = "Import", dynamicConfig = EruptRecordNaming.class)
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    @Transactional
    public R<Void> importData(@PathVariable("erupt") String eruptName, @RequestParam("file") MultipartFile file) {
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, PowerObject::isImportable, "Not import permission");
        if (file.isEmpty() || null == file.getOriginalFilename()) return R.errorDialog("No file");
        if (!xlsxCodec.accept(file.getOriginalFilename())) {
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.unsupported_format"), file.getOriginalFilename()));
        }
        List<JsonObject> list;
        try (InputStream in = file.getInputStream()) {
            list = excelService.records(eruptModel, xlsxCodec.read(eruptModel, in));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.parse_error"), xlsxCodec.name(), e.getMessage()), e);
        }
        try {
            List<Object> eruptDataList = new ArrayList<>();
            int row = 1;
            for (JsonObject data : list) {
                row++;
                R<Void> validation = EruptUtil.validateEruptValue(eruptModel, data);
                if (!validation.isSuccess()) {
                    throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.row_error"), row, validation.getMessage()));
                }
                eruptDataList.add(eruptModifyService.eruptInsertDataProcess(eruptModel, data));
            }
            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.excelImportProcess(eruptDataList)));
            eruptModifyController.batchAddEruptData(eruptModel, eruptDataList);
        } catch (Exception e) {
            log.error("import error {}", eruptModel.getEruptName(), e);
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.import_error"), e.getMessage()));
        }
        return R.ok();
    }

}
