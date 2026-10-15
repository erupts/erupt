package xyz.erupt.cloud.server.service;

import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import xyz.erupt.cloud.common.http.CloudSignature;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.RemoteRequestValidator;
import xyz.erupt.core.util.SecurityUtil;
import xyz.erupt.core.view.EruptBuildModel;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Excel template, export and import of a node's erupt, for a node that leaves them to the server (404): the
 * sheets are written and read by erupt-excel against the build model ({@link EruptModel#remote}), the rows
 * are read and written through the node's table and add endpoints. Rows are added one by one, so an import
 * stops at the first failing row with the rows before it kept, and says how many.
 *
 * @author YuePeng
 * date 2026/10/15
 */
@Service
public class NodeExcelService {

    private static final Set<CloudSignature.Enforced> QUERIED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.QUERY);

    private static final Set<CloudSignature.Enforced> RECORDED = EnumSet.of(CloudSignature.Enforced.POWER, CloudSignature.Enforced.RECORD);

    private static final Set<CloudSignature.Enforced> VOUCHED = EnumSet.of(CloudSignature.Enforced.POWER);

    private static final int EXPORT_PAGE_SIZE = 1000;

    // the export / import endpoints' request parameters and form field
    private static final String IDS = "ids";

    private static final String FORMAT = "format";

    private static final String FILE = "file";

    private static final String IMPORTED = "Rows imported before the failure: %s";

    @Resource
    private CloudNodeRouter cloudNodeRouter;

    @Resource
    private NodeReferenceService nodeReferenceService;

    @Resource
    private EruptExcelService eruptExcelService;

    @Resource
    private TableCodecs tableCodecs;

    @Resource
    private XlsxCodec xlsxCodec;

    public void template(String erupt, EruptBuildModel build, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SecurityUtil.csrfInspect(request, response)) return;
        EruptModel model = EruptModel.remote(erupt, node(erupt), build);
        TableCodec codec = tableCodecs.get(null);
        codec.write(eruptExcelService.template(model),
                ExcelUtil.downLoadFile(request, response, model.getErupt().name() + "_template." + codec.format()));
    }

    /**
     * Everything the query matches, page by page through the node, or only the selected ids.
     */
    public void export(String erupt, EruptBuildModel build, byte[] body, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SecurityUtil.csrfInspect(request, response)) return;
        String node = node(erupt);
        EruptModel model = EruptModel.remote(erupt, node, build);
        TableCodec codec = tableCodecs.get(request.getParameter(FORMAT));
        TableQuery query = null == body || body.length == 0 ? new TableQuery()
                : GsonFactory.getGson().fromJson(new String(body, StandardCharsets.UTF_8), TableQuery.class);
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> ids = ids(request);
        if (ids.isEmpty()) {
            query.setPageSize(EXPORT_PAGE_SIZE);
            for (int index = 1; rows.size() < Page.PAGE_MAX_DATA; index++) {
                query.setPageIndex(index);
                Page page = page(nodeReferenceService.resolve(node, build, true, cloudNodeRouter.proxy(erupt, HttpMethod.POST,
                        EruptRestPath.ERUPT_DATA + "/table" + EruptRestPath.ERUPT_NAME_HOLDER, query, QUERIED)));
                Collection<Map<String, Object>> list = Optional.ofNullable(page.getList()).orElse(List.of());
                rows.addAll(list);
                if (list.size() < EXPORT_PAGE_SIZE) break;
            }
        } else {
            for (String id : ids) {
                rows.add(GsonFactory.getGson().fromJson(nodeReferenceService.resolve(node, build, false, cloudNodeRouter.proxy(erupt, HttpMethod.GET,
                        EruptRestPath.ERUPT_DATA + EruptRestPath.ERUPT_NAME_HOLDER + "/" + UriUtils.encodePathSegment(id, StandardCharsets.UTF_8),
                        null, VOUCHED)), Map.class));
            }
        }
        Page page = new Page();
        page.setList(rows);
        String fileName = model.getErupt().name() + "_" + DateUtil.getFormatDate(new Date(), DateUtil.ISO_8601) + "." + codec.format();
        codec.write(eruptExcelService.sheet(model, page, query.getCondition()), ExcelUtil.downLoadFile(request, response, fileName));
    }

    /**
     * The rows of an Excel file, checked as a submitted record is and added through the node one by one.
     */
    public R<Void> importData(String erupt, EruptBuildModel build, HttpServletRequest request) throws IOException {
        String node = node(erupt);
        EruptModel model = EruptModel.remote(erupt, node, build);
        Part file;
        try {
            file = request.getPart(FILE);
        } catch (ServletException e) {
            return R.errorDialog("No file");
        }
        if (null == file || file.getSize() == 0 || null == file.getSubmittedFileName()) return R.errorDialog("No file");
        if (!xlsxCodec.accept(file.getSubmittedFileName())) {
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.unsupported_format"), file.getSubmittedFileName()));
        }
        List<JsonObject> records;
        try (InputStream in = file.getInputStream()) {
            records = eruptExcelService.records(model, xlsxCodec.read(model, in), field -> nodeReferenceService.labels(node, field));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(String.format(I18nTranslate.$translate("excel.parse_error"), xlsxCodec.name(), e.getMessage()), e);
        }
        for (int i = 0; i < records.size(); i++) {
            R<?> problem = RemoteRequestValidator.validateRecord(build, records.get(i));
            if (null == problem) {
                try {
                    problem = GsonFactory.getGson().fromJson(cloudNodeRouter.proxy(erupt, HttpMethod.POST,
                            EruptRestPath.ERUPT_DATA_MODIFY + EruptRestPath.ERUPT_NAME_HOLDER, records.get(i), RECORDED), R.class);
                } catch (EruptWebApiRuntimeException e) {
                    problem = R.error(e.getMessage());
                }
                if (problem.isSuccess()) continue;
            }
            // the header is row 1
            return R.errorDialog(String.format(I18nTranslate.$translate("excel.row_error"), i + 2, problem.getMessage())
                    + "\n" + String.format(I18nTranslate.$translate(IMPORTED), i));
        }
        return R.ok();
    }

    private static Page page(String json) {
        return GsonFactory.getGson().fromJson(json, Page.class);
    }

    // "ids=1&ids=2" or "ids=1,2", as Spring binds the endpoint's List parameter
    private static List<String> ids(HttpServletRequest request) {
        List<String> ids = new ArrayList<>();
        for (String value : Optional.ofNullable(request.getParameterValues(IDS)).orElse(new String[0])) {
            for (String id : value.split(",")) if (!id.isBlank()) ids.add(id.trim());
        }
        return ids;
    }

    private static String node(String erupt) {
        return erupt.substring(0, erupt.lastIndexOf(EruptConst.DOT));
    }

}
