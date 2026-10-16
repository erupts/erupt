package xyz.erupt.core.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.HtmlEditorType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.EruptFileService;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.R;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Files
 *
 * @order 11
 * @apiNote Attachment upload and download.
 *
 * @author YuePeng
 * date 10/15/18.
 */
@Slf4j
@RestController
@RequestMapping(EruptRestPath.ERUPT_FILE)
@RequiredArgsConstructor
public class EruptFileController {

    private final EruptProp eruptProp;

    private final EruptFileService eruptFileService;

    private static final String FS_SEP = "/";

    /**
     * Upload one file for an ATTACHMENT or HTML_EDITOR field.
     *
     * @apiNote Multipart form field "file". Extension, size and image dimensions are checked against @AttachmentType; the stored path is returned and is what the record field should hold.
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type ATTACHMENT or HTML_EDITOR
     * @param file File to store
     * @return stored path
     */
    @SneakyThrows
    @PostMapping("/upload/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT}, resolve = EruptRouter.Resolve.UPLOAD)
    public R<String> upload(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile file) {
        // Generating storage paths
        if (null == file.getOriginalFilename()) {
            return R.errorDialog(I18nTranslate.$translate("filename is empty"));
        }
        // an erupt-cloud node erupt is uploaded here too, by the rules of its node's build: the download serves it from here
        EruptModel eruptModel = EruptCoreService.getEruptWithRemoteModel(eruptName);
        EruptFieldModel fieldModel = null == eruptModel ? null : eruptModel.getEruptFieldMap().get(fieldName);
        if (null == fieldModel) {
            throw new EruptWebApiRuntimeException("field not found: " + fieldName);
        }
        Edit edit = fieldModel.getEruptField().edit();
        String path = eruptFileService.createPath(file).replace(edit.attachmentType().fileSeparator(), "");
        switch (edit.type()) {
            case ATTACHMENT:
                AttachmentType attachmentType = edit.attachmentType();
                // Validates the extension
                if (attachmentType.fileTypes().length > 0) {
                    String[] fileNameArr = file.getOriginalFilename().split("\\.");
                    String extensionName = fileNameArr[fileNameArr.length - 1];
                    if (Stream.of(attachmentType.fileTypes()).noneMatch(extensionName::equalsIgnoreCase)) {
                        return R.errorDialog(I18nTranslate.$translate("erupt.upload_error.file_format") + ": " + extensionName);
                    }
                }
                if (!"".equals(attachmentType.path())) {
                    path = attachmentType.path() + path;
                }
                // Verify the file size
                if (attachmentType.size() > 0 && file.getSize() / 1024 > attachmentType.size()) {
                    return R.errorDialog(I18nTranslate.$translate("erupt.upload_error.size") + ": " + attachmentType.size() + "KB");
                }
                switch (edit.attachmentType().type()) {
                    case IMAGE:
                        AttachmentType.ImageType imageType = edit.attachmentType().imageType();
                        boolean hasDimensionConstraint = imageType.minWidth() > 0 || imageType.maxWidth() < Integer.MAX_VALUE
                                || imageType.minHeight() > 0 || imageType.maxHeight() < Integer.MAX_VALUE;
                        if (hasDimensionConstraint) {
                            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                            if (bufferedImage == null) {
                                return R.errorDialog(I18nTranslate.$translate("erupt.upload_error.not_image"));
                            }
                            int width = bufferedImage.getWidth();
                            int height = bufferedImage.getHeight();
                            if (imageType.minWidth() > width || imageType.maxWidth() < width) {
                                return R.errorDialog(I18nTranslate.$translate("erupt.upload_error.image_width") + String.format("[%s,%s]", imageType.minWidth(), imageType.maxWidth()));
                            }
                            if (imageType.minHeight() > height || imageType.maxHeight() < height) {
                                return R.errorDialog(I18nTranslate.$translate("erupt.upload_error.image_height") + String.format("[%s,%s]", imageType.minHeight(), imageType.maxHeight()));
                            }
                        }
                        break;
                    case BASE:
                        break;
                }
                break;
            case HTML_EDITOR:
                HtmlEditorType htmlEditorType = edit.htmlEditorType();
                if (!"".equals(htmlEditorType.path())) {
                    path = htmlEditorType.path() + path;
                }
                break;
        }
        return R.ok(eruptFileService.upload(file, path));

    }


    /**
     * Upload several files for one field.
     *
     * @apiNote Same rules as the single upload, applied to every part named "file".
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type ATTACHMENT
     * @param files Files to store
     * @return stored paths in request order
     */
    @PostMapping("/uploads/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT}, resolve = EruptRouter.Resolve.UPLOAD)
    public R<List<String>> uploads(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            R<String> r = upload(eruptName, fieldName, file);
            paths.add(r.getData());
        }
        return R.ok(paths);
    }


    /**
     * Image upload for a rich text editor without a bound field.
     *
     * @apiNote Multipart form field "upload"; the token travels in the _token query parameter because editors cannot set headers. Response shape follows the CKEditor upload adapter.
     * @param file Image to store
     * @return url, uploaded and state
     */
    @PostMapping("/upload-editor")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN, verifyMethod = EruptRouter.VerifyMethod.PARAM)
    public Map<String, Object> uploadEditor(@RequestParam("upload") MultipartFile file) {
        String path = eruptFileService.upload(file, eruptFileService.createPath(file));
        Map<String, Object> map = new HashMap<>(4);
        AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
        map.put("url", null != attachmentProxy ? attachmentProxy.fileDomain() + path : EruptRestPath.ERUPT_ATTACHMENT + path);
        map.put("uploaded", true);
        map.put("state", "SUCCESS");
        return map;
    }

    /**
     * Image upload for an HTML_EDITOR field.
     *
     * @apiNote Multipart form field "upload"; token in the _token query parameter. The url is prefixed with the AttachmentProxy file domain when one is registered.
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type HTML_EDITOR
     * @param file Image to store
     * @return url, uploaded and state
     */
    @PostMapping("/upload-html-editor/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyMethod = EruptRouter.VerifyMethod.PARAM, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT}, resolve = EruptRouter.Resolve.UPLOAD)
    public Map<String, Object> uploadHtmlEditorImage(@PathVariable("erupt") String eruptName,
                                                     @PathVariable("field") String fieldName,
                                                     @RequestParam("upload") MultipartFile file) throws ClassNotFoundException {
        R<String> r = upload(eruptName, fieldName, file);
        Map<String, Object> map = new HashMap<>(2);
        if (r.isSuccess()) {
            //{"uploaded":"true", "url":"image-path..."}
            AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
            if (null != attachmentProxy) {
                map.put("url", attachmentProxy.fileDomain() + r.getData());
            } else {
                map.put("url", EruptRestPath.ERUPT_ATTACHMENT + r.getData());
            }
            map.put("uploaded", true);
            map.put("state", "SUCCESS");
        } else {
            map.put("uploaded", false);
            map.put("state", "ERROR");
            throw new EruptWebApiRuntimeException(r.getMessage());
        }
        return map;
    }


    /**
     * UEditor endpoint: configuration and image upload.
     *
     * @apiNote Without a file it returns the UEditor config (optionally wrapped in the JSONP callback); with a multipart "file" part it behaves like upload-html-editor and answers in the UEditor response format. Token in the _token query parameter.
     * @param eruptName Simple class name of the @Erupt model
     * @param fieldName Field name with edit type HTML_EDITOR
     * @param callback JSONP callback name for the configuration request
     * @param file Image to store
     */
    @RequestMapping("/upload-ueditor/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyMethod = EruptRouter.VerifyMethod.PARAM, verifyType = EruptRouter.VerifyType.ERUPT, power = {EruptRouter.Power.ADD, EruptRouter.Power.EDIT}, resolve = EruptRouter.Resolve.UPLOAD)
    public void uploadUEditorImage(@PathVariable("erupt") String eruptName,
                                   @PathVariable("field") String fieldName,
                                   @RequestParam(value = "callback", required = false) String callback,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   HttpServletResponse response) throws IOException, ClassNotFoundException {
        if (null == file) {
            @Cleanup InputStream stream = EruptFileController.class.getClassLoader().getResourceAsStream("ueditor.json");
            String json = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
            if (null == callback) {
                response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            } else {
                response.getOutputStream().write((callback + "(" + json + ")").getBytes(StandardCharsets.UTF_8));
            }
        } else {
            Map<String, Object> map = this.uploadHtmlEditorImage(eruptName, fieldName, file);
            map.put("filename", file.getName());
            map.put("original", file.getOriginalFilename());
            map.put("name", file.getName());
            map.put("size", file.getSize());
            response.getOutputStream().write(new Gson().toJson(map).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static final String DOWNLOAD_PATH = "/download-attachment";

    /**
     * Download an uploaded file.
     *
     * @apiNote The part of the path after download-attachment is the stored path returned by upload, resolved under the configured upload directory. No session is needed.
     * @download
     */
    @GetMapping(value = DOWNLOAD_PATH + "/**", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void downloadAttachment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath().replace(EruptRestPath.ERUPT_FILE + DOWNLOAD_PATH, "");
        if (!path.startsWith(FS_SEP)) {
            path = FS_SEP + path;
        }
        Path uploadRoot = Paths.get(eruptProp.getUploadPath());
        Path target = uploadRoot.resolve(path.substring(1)).normalize();
        if (!target.startsWith(uploadRoot)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal path");
            return;
        }
        if (!Files.isRegularFile(target) || !Files.exists(target)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + target.getFileName() + "\"");
        Files.copy(target, response.getOutputStream());
        response.flushBuffer();
    }

}
