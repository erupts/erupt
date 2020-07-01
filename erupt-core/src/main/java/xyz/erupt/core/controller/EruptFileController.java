package xyz.erupt.core.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.config.EruptConfig;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.MimeUtil;
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
import java.util.*;

/**
 * @author liyuepeng
 * @date 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_FILE)
public class EruptFileController {

    @Autowired
    private EruptConfig eruptConfig;

    private static final String FS_SEP = "/";

    @PostMapping("/upload/{erupt}/{field}")
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel upload(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || StringUtils.isBlank(file.getOriginalFilename())) {
            return EruptApiModel.errorApi("上传失败，请选择文件");
        }
        try {
            //生成存储路径
            EruptModel eruptModel = EruptCoreService.getErupt(eruptName);
            if (!eruptModel.getErupt().power().edit() && !eruptModel.getErupt().power().add()) {
                throw new EruptNoLegalPowerException();
            }
            Edit edit = eruptModel.getEruptFieldMap().get(fieldName).getEruptField().edit();
            String path = DateUtil.getFormatDate(new Date(), DateUtil.DATE) + File.separator +
                    RandomUtils.nextInt(100, 9999) + "-" +
                    file.getOriginalFilename()
                            .replace("&", "")
                            .replace("?", "")
                            .replace("#", "")
                            .replace(" ", "")
                            .replace(edit.attachmentType().fileSeparator(), "");
            switch (edit.type()) {
                case ATTACHMENT:
                    AttachmentType attachmentType = edit.attachmentType();
                    //校验扩展名
                    if (attachmentType.fileTypes().length > 0) {
                        String[] fileNameArr = file.getOriginalFilename().split("\\.");
                        String extensionName = fileNameArr[fileNameArr.length - 1];
                        if (!Arrays.asList(attachmentType.fileTypes()).contains(extensionName)) {
                            return EruptApiModel.errorApi("上传失败，文件格式不允许为：" + extensionName);
                        }
                    }

                    if (!"".equals(attachmentType.path())) {
                        path = attachmentType.path() + path;
                    }
                    //校验文件大小
                    if (attachmentType.size() > 0 && file.getSize() / 1024 > attachmentType.size()) {
                        return EruptApiModel.errorApi("上传失败，文件大小不能超过" + attachmentType.size() + "KB");
                    }
                    switch (edit.attachmentType().type()) {
                        case IMAGE:
                            AttachmentType.ImageType imageType = edit.attachmentType().imageType();
                            // 通过MultipartFile得到InputStream，从而得到BufferedImage
                            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                            if (bufferedImage == null) {
                                return EruptApiModel.errorApi("获取图片流失败，请确认上传文件为图片");
                            }
                            if (imageType.width().length > 1 || imageType.height().length > 1) {
                                int width = bufferedImage.getWidth();
                                int height = bufferedImage.getHeight();
                                if (imageType.width().length > 1) {
                                    if (imageType.width()[0] > width || imageType.width()[1] < width) {
                                        return EruptApiModel.errorApi("上传失败，图片宽度不在["
                                                + imageType.width()[0] + "," + imageType.width()[1] + "]范围内");
                                    }
                                }
                                if (imageType.height().length > 1) {
                                    if (imageType.height()[0] > height || imageType.height()[1] < height) {
                                        return EruptApiModel.errorApi("上传失败，图片高度不在["
                                                + imageType.height()[0] + "," + imageType.height()[1] + "]范围内");
                                    }
                                }
                            }
                            break;
                        case OTHER:

                            break;
                    }
                    break;
                case HTML_EDIT:
                    break;
                default:
                    return EruptApiModel.errorApi("上传失败，非法类型!");
            }
            boolean localSave = true;
            if (StringUtils.isNotBlank(eruptConfig.getAttachmentProxy())) {
                AttachmentProxy attachmentProxy = EruptSpringUtil.getBeanByPath(eruptConfig.getAttachmentProxy(), AttachmentProxy.class);
                attachmentProxy.upLoad(file.getInputStream(), path.replace("\\", "/"));
                localSave = attachmentProxy.isLocalSave();
            }
            if (localSave) {
                File dest = new File(eruptConfig.getUploadPath() + File.separator + path);
                if (!dest.getParentFile().exists()) {
                    if (!dest.getParentFile().mkdirs()) {
                        return EruptApiModel.errorApi("上传失败，文件目录无法创建");
                    }
                }
                file.transferTo(dest);
            }
            return EruptApiModel.successApi(path.replace("\\", "/"));
        } catch (Exception e) {
            e.printStackTrace();
            return EruptApiModel.errorApi("上传失败，" + e.getMessage());
        }
    }

    private static final String DOWNLOAD_PATH = "/download-attachment";


    @PostMapping("/uploads/{erupt}/{field}")
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)
    public EruptApiModel uploads(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            EruptApiModel eruptApiModel = upload(eruptName, fieldName, file);
            paths.add(eruptApiModel.getMessage());
        }
        return EruptApiModel.successApi(String.join(",", paths));
    }

    private static final String PREVIEW_PATH = "/preview-attachment";

    @PostMapping("/upload-html-editor/{erupt}/{field}")
    @ResponseBody
    @EruptRouter(authIndex = 2, verifyMethod = EruptRouter.VerifyMethod.PARAM, verifyType = EruptRouter.VerifyType.ERUPT)
    public Map<String, Object> uploadHtmlEditorImage(@PathVariable("erupt") String eruptName,
                                                     @PathVariable("field") String fieldName,
                                                     @RequestParam("upload") MultipartFile file,
                                                     HttpServletRequest request) throws ClassNotFoundException {
        EruptApiModel eruptApiModel = upload(eruptName, fieldName, file);
        Map<String, Object> map = new HashMap<>(2);
        if (eruptApiModel.getStatus() == EruptApiModel.Status.SUCCESS) {
            //{"uploaded":"true", "url":"image-path..."}
            if (StringUtils.isNotBlank(eruptConfig.getAttachmentProxy())) {
                AttachmentProxy attachmentProxy = EruptSpringUtil.getBeanByPath(eruptConfig.getAttachmentProxy(), AttachmentProxy.class);
                map.put("url", attachmentProxy.fileDomain() + "/" + eruptApiModel.getData());
            } else {
                //request.getRequestURL().toString().split(RestPath.ERUPT_API)[0] +
                map.put("url", RestPath.ERUPT_FILE + PREVIEW_PATH + "/" + eruptApiModel.getData());
            }
            map.put("uploaded", true);
        } else {
            map.put("uploaded", false);
        }
        return map;
    }

    @RequestMapping(value = DOWNLOAD_PATH + "/**", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    public byte[] downloadAttachment(HttpServletRequest request, HttpServletResponse response) {
        return mappingFileToByte(request.getServletPath().replace(RestPath.ERUPT_FILE + DOWNLOAD_PATH, ""), response);
    }


    @RequestMapping(value = {PREVIEW_PATH + "/**"})
    @ResponseBody
    public void previewAttachment(HttpServletResponse response, HttpServletRequest request) {
        previewAttachment(request.getServletPath().replace(RestPath.ERUPT_FILE + PREVIEW_PATH, ""), response);
    }


    @RequestMapping(value = PREVIEW_PATH)
    @ResponseBody
    public void previewAttachment(@RequestParam("path") String path, HttpServletResponse response) {
        int cacheTime = 60 * 60 * 24 * 10 * 1000;
        response.addHeader("cache-control", "max-age=" + cacheTime);
        response.setDateHeader("expires", (System.currentTimeMillis() + cacheTime));
        response.setDateHeader("date", System.currentTimeMillis());
        response.setHeader("etag", RandomStringUtils.randomAlphabetic(10));
        response.setDateHeader("Last-Modified", System.currentTimeMillis() - 1000 * 60);
        response.setContentType(MimeUtil.getMimeType(path));
        try {
            IOUtils.write(mappingFileToByte(path, response), response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] mappingFileToByte(String path, HttpServletResponse response) {
        if (!path.startsWith(FS_SEP)) {
            path = FS_SEP + path;
        }
        File file = new File(eruptConfig.getUploadPath() + path);
        try {
            InputStream inputStream;
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = MimeUtil.class.getClassLoader().getResourceAsStream("empty.png");
                response.setContentType("image/png");
                response.setHeader("Content-Disposition", "filename=empty.png");
            }
            return StreamUtils.copyToByteArray(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

}
