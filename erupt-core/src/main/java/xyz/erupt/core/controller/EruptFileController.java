package xyz.erupt.core.controller;

import com.google.gson.Gson;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
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
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.core.view.EruptModel;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 10/15/18.
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_FILE)
@RequiredArgsConstructor
public class EruptFileController {

    private final EruptProp eruptProp;

    private static final String FS_SEP = "/";

    @SneakyThrows
    @PostMapping("/upload/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel upload(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile file) {
        //生成存储路径
        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isEdit() || powerObject.isAdd());
        Edit edit = eruptModel.getEruptFieldMap().get(fieldName).getEruptField().edit();
        String path;
        if (eruptProp.isKeepUploadFileName()) {
            path = File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE) + File.separator + file.getOriginalFilename()
                    .replaceAll("&|#|\\?|\\s", "").replace(edit.attachmentType().fileSeparator(), "");
        } else {
            String[] fileNameSplit = file.getOriginalFilename().split("\\.");
            path = File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE)
                    + File.separator + RandomStringUtils.randomAlphabetic(12) + "." + fileNameSplit[fileNameSplit.length - 1];
        }
        switch (edit.type()) {
            case ATTACHMENT:
                AttachmentType attachmentType = edit.attachmentType();
                //校验扩展名
                if (attachmentType.fileTypes().length > 0) {
                    String[] fileNameArr = file.getOriginalFilename().split("\\.");
                    String extensionName = fileNameArr[fileNameArr.length - 1];
                    if (Stream.of(attachmentType.fileTypes()).noneMatch(type -> extensionName.equalsIgnoreCase(type))) {
                        return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.file_format", extensionName));
                    }
                }
                if (!"".equals(attachmentType.path())) {
                    path = attachmentType.path() + path;
                }
                //校验文件大小
                if (attachmentType.size() > 0 && file.getSize() / 1024 > attachmentType.size()) {
                    return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.size", attachmentType.size() + "KB"));
                }
                switch (edit.attachmentType().type()) {
                    case IMAGE:
                        AttachmentType.ImageType imageType = edit.attachmentType().imageType();
                        // 通过MultipartFile得到InputStream，从而得到BufferedImage
                        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                        if (bufferedImage == null) {
                            return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.not_image"));
                        }
                        int width = bufferedImage.getWidth();
                        int height = bufferedImage.getHeight();
                        if (imageType.minWidth() > width || imageType.maxWidth() < width) {
                            return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.image_width", imageType.minWidth() + "," + imageType.maxWidth()));
                        }
                        if (imageType.minHeight() > height || imageType.maxHeight() < height) {
                            return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.image_height", imageType.minHeight() + "," + imageType.maxHeight()));
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
        try {
            boolean localSave = true;
            AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
            if (null != attachmentProxy) {
                path = attachmentProxy.upLoad(file.getInputStream(), path.replace("\\", "/"));
                localSave = attachmentProxy.isLocalSave();
            }
            if (localSave) {
                File dest = new File(eruptProp.getUploadPath() + path);
                if (!dest.getParentFile().exists()) {
                    if (!dest.getParentFile().mkdirs()) {
                        return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error.cannot_created"));
                    }
                }
                file.transferTo(dest);
            }
            return EruptApiModel.successApi(path.replace("\\", "/"));
        } catch (Exception e) {
            e.printStackTrace();
            return EruptApiModel.errorApi(I18nTranslate.$translate("erupt.upload_error") + " " + e.getMessage());
        }
    }


    @PostMapping("/uploads/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel uploads(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            EruptApiModel eruptApiModel = upload(eruptName, fieldName, file);
            paths.add(eruptApiModel.getMessage());
        }
        return EruptApiModel.successApi(String.join(",", paths));
    }


    @PostMapping("/upload-html-editor/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyMethod = EruptRouter.VerifyMethod.PARAM, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> uploadHtmlEditorImage(@PathVariable("erupt") String eruptName,
                                                     @PathVariable("field") String fieldName,
                                                     @RequestParam("upload") MultipartFile file) throws ClassNotFoundException {
        EruptApiModel eruptApiModel = upload(eruptName, fieldName, file);
        Map<String, Object> map = new HashMap<>(2);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.SUCCESS) {
            //{"uploaded":"true", "url":"image-path..."}
            AttachmentProxy attachmentProxy = EruptUtil.findAttachmentProxy();
            if (null != attachmentProxy) {
                map.put("url", attachmentProxy.fileDomain() + eruptApiModel.getData());
            } else {
                map.put("url", EruptRestPath.ERUPT_ATTACHMENT + eruptApiModel.getData());
            }
            map.put("uploaded", true);
        } else {
            map.put("uploaded", false);
            throw new EruptWebApiRuntimeException(eruptApiModel.getMessage());
        }
        return map;
    }


    @RequestMapping("/upload-ueditor/{erupt}/{field}")
    @EruptRouter(authIndex = 2, verifyMethod = EruptRouter.VerifyMethod.PARAM, verifyType = EruptRouter.VerifyType.ERUPT)
    public void uploadUEditorImage(@PathVariable("erupt") String eruptName,
                                   @PathVariable("field") String fieldName,
                                   @RequestParam(value = "callback", required = false) String callback,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   HttpServletResponse response) throws IOException, ClassNotFoundException {
        if (null == file) {
            @Cleanup InputStream stream = EruptFileController.class.getClassLoader().getResourceAsStream("ueditor.json");
            String json = StreamUtils.copyToString(stream, Charset.forName(StandardCharsets.UTF_8.name()));
            if (null == callback) {
                response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            } else {
                response.getOutputStream().write((callback + "(" + json + ")").getBytes(StandardCharsets.UTF_8));
            }
        } else {
            Map<String, Object> map = uploadHtmlEditorImage(eruptName, fieldName, file);
            Boolean status = (Boolean) map.get("uploaded");
            map.put("state", status ? "SUCCESS" : "ERROR");
            response.getOutputStream().write(new Gson().toJson(map).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final String DOWNLOAD_PATH = "/download-attachment";

    @GetMapping(value = DOWNLOAD_PATH + "/**", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void downloadAttachment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath().replace(EruptRestPath.ERUPT_FILE + DOWNLOAD_PATH, "");
        if (!path.startsWith(FS_SEP)) {
            path = FS_SEP + path;
        }
        File file = new File(eruptProp.getUploadPath() + path);
        if (!file.exists()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }
        @Cleanup InputStream inputStream = new FileInputStream(file);
        response.getOutputStream().write(StreamUtils.copyToByteArray(inputStream));
    }

}
