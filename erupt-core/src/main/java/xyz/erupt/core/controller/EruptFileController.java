package xyz.erupt.core.controller;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.AttachmentType;
import xyz.erupt.annotation.sub_field.sub_edit.sub_attachment.ImageType;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptApiModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.SpringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyuepeng on 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_FILE)
public class EruptFileController {

    @Value("${erupt.uploadPath:/opt/file}")
    private String uploadPath;

    @PostMapping("/upload/{erupt}/{field}")
    @ResponseBody
    public EruptApiModel upload(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return EruptApiModel.errorApi("上传失败，请选择文件");
        }
        try {
            //生成存储路径
            String path = File.separator + DateUtil.getFormatDate(DateUtil.DATE) + File.separator + RandomUtils.nextInt(100, 9999) + "-" + file.getOriginalFilename();
            EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
            Edit edit = eruptModel.getEruptFieldMap().get(fieldName).getEruptField().edit();
            switch (edit.type()) {
                case ATTACHMENT:
                    AttachmentType attachmentType = edit.attachmentType()[0];
                    //校验扩展名
                    if (attachmentType.fileTypes().length > 0) {
                        String[] fileNameArr = file.getOriginalFilename().split("\\.");
                        String extensionName = fileNameArr[fileNameArr.length - 1];
                        boolean legalExtension = false;
                        for (String s : attachmentType.fileTypes()) {
                            if (s.equals(extensionName)) {
                                legalExtension = true;
                                break;
                            }
                        }
                        if (!legalExtension) {
                            return EruptApiModel.errorApi("上传失败，文件格式不允许为：" + extensionName);
                        }
                    }

                    if (!"".equals(attachmentType.path())) {
                        if (attachmentType.path().startsWith(File.separator)) {
                            path = attachmentType.path() + path;
                        } else {
                            path = "/" + attachmentType.path() + path;
                        }
                    }
                    //校验文件大小
                    if (attachmentType.size() > 0 && file.getSize() / 1000 > attachmentType.size()) {
                        return EruptApiModel.errorApi("上传失败，文件大小不能超过" + attachmentType.size() + "KB");
                    }
                    switch (edit.attachmentType()[0].type()) {
                        case IMAGE:
                            ImageType imageType = edit.attachmentType()[0].imageType();
                            BufferedImage bufferedImage = ImageIO.read(file.getInputStream()); // 通过MultipartFile得到InputStream，从而得到BufferedImage
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
                        case VIDEO:

                            break;
                    }
                    break;
                case HTML_EDIT:
                    break;
                default:
                    return EruptApiModel.errorApi("上传失败，非法类型!");
            }
            File dest = new File(uploadPath + File.separator + path);
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    return EruptApiModel.errorApi("上传失败，文件目录无法创建");
                }
            }
            //执行upload proxy
            for (Class<? extends DataProxy> clazz : eruptModel.getErupt().dateProxy()) {
                BoolAndReason boolAndReason = SpringUtil.getBean(clazz).upLoadFile(file.getInputStream(), dest);
                if (!boolAndReason.isBool()) {
                    return new EruptApiModel(boolAndReason);
                }
            }
            file.transferTo(dest);
            return EruptApiModel.successApi(path);
        } catch (Exception e) {
            return EruptApiModel.errorApi("上传失败，" + e.getMessage());
        }
    }

    @PostMapping("/uploads/{erupt}/{field}")
    @ResponseBody
    public EruptApiModel uploads(@PathVariable("erupt") String eruptName, @PathVariable("field") String fieldName, @RequestParam("file") MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            EruptApiModel eruptApiModel = upload(eruptName, fieldName, file);
            paths.add(eruptApiModel.getMessage());
        }
        return EruptApiModel.successApi(String.join(",", paths));
    }

//    @RequestMapping(value = "/{erupt}/preview-attachment",
//            produces = {MediaType.IMAGE_JPEG_VALUE,MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
//    @ResponseBody
//    public byte[] preview(@RequestParam("path") String path, @PathVariable("erupt") String eruptName,
//                          HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if (!path.startsWith(File.separator)) {
//            path = File.separator + path;
//        }
//        File file = new File(uploadPath + path);
//        if (!file.exists()) {
//            file = new File(this.getClass().getResource("not_img.png").getFile());
//        }
//        FileInputStream inputStream = new FileInputStream(file);
//        byte[] bytes = new byte[inputStream.available()];
//        inputStream.read(bytes, 0, inputStream.available());
//        return bytes;
//    }

//    @GetMapping("/download-attachment")
//    @ResponseBody
//    public ResponseEntity<byte[]> downloadAttachment(@RequestParam("path") String path, HttpServletRequest request, HttpServletResponse response) {
//        String[] pathSplits = path.split("/");
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        try {
//            headers.setContentDispositionFormData("attachment", java.net.URLEncoder.encode(pathSplits[pathSplits.length - 1], "UTF-8"));
//            File file = new File(this.uploadPath + File.separator + path);
//            return new ResponseEntity<byte[]>(FileUtil.readAsByteArray(file),
//                    headers, HttpStatus.CREATED);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


}
