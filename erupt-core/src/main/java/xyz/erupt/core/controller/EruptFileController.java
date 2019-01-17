package xyz.erupt.core.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.sub_edit.ImageType;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.model.EruptApiModel;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.DateUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by liyuepeng on 10/15/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_FILE)
public class EruptFileController {

    @Value("${erupt.uploadPath}")
    private String uploadPath;

    @PostMapping("/upload/{erupt}/{field}")
    @ResponseBody
    public EruptApiModel upload(@PathVariable("erupt") String eruptName,
                                @PathVariable("field") String fieldName,
                                @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return EruptApiModel.errorApi("上传失败，请选择文件");
        }
        String path = DateUtil.getFormatDate(DateUtil.DATE) + File.separator + file.getOriginalFilename();
        File dest = new File(uploadPath + File.separator + path);
        if (!dest.exists()) {
            if (!dest.getParentFile().mkdirs()) {
                return EruptApiModel.errorApi("上传失败，文件目录无法创建");
            }
        }
        try {
            EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
            Edit edit = eruptModel.getEruptFieldMap().get(fieldName).getEruptField().edit();
            switch (edit.type()) {
                case IMAGE:
                    ImageType imageType = edit.imageType()[0];
                    if (imageType.size() != 0 && file.getSize() > imageType.size()) {
                        return EruptApiModel.errorApi("上传失败，文件大小不能超过" + imageType.size());
                    }
                    if (imageType.height() > 0 || imageType.height() > 0) {
                        BufferedImage bufferedImage = ImageIO.read(file.getInputStream()); // 通过MultipartFile得到InputStream，从而得到BufferedImage
                        if (bufferedImage == null) {
                            return EruptApiModel.errorApi("上传失败，获取图片流失败，请确认上传文件为图片");
                        } else {
                            int width = bufferedImage.getWidth();
                            int height = bufferedImage.getHeight();
                            if (imageType.width() > 0) {
                                if (width > imageType.width()) {
                                    return EruptApiModel.errorApi("上传失败，图片宽度超过" + imageType.width());
                                } else if (imageType.vagueWidth() && width != imageType.width()) {
                                    return EruptApiModel.errorApi("上传失败，图片宽度不等于" + imageType.width());
                                }
                            }
                            if (imageType.height() > 0) {
                                if (height > bufferedImage.getHeight()) {
                                    return EruptApiModel.errorApi("上传失败，图片高度超过" + imageType.height());
                                } else if (imageType.vagueHeight() && height != imageType.height()) {
                                    return EruptApiModel.errorApi("上传失败，图片高度不等于" + imageType.width());
                                }

                            }
                        }
                    }
                    break;
                case FILE:

                    break;
            }
            //执行upload proxy
            for (Class<? extends DataProxy> clazz : eruptModel.getErupt().dateProxy()) {
                BoolAndReason boolAndReason = clazz.newInstance().upLoadFile(file.getInputStream(), file.getOriginalFilename());
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


}
