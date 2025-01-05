package xyz.erupt.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;

/**
 * @author YuePeng
 * date 2024/7/28 22:20
 */
@Service
@Slf4j
public class EruptFileService {

    @Resource
    private EruptProp eruptProp;

    public String createPath(MultipartFile file) {
        if (file.isEmpty() || StringUtils.isBlank(file.getOriginalFilename())) {
            throw new EruptWebApiRuntimeException("not found file");
        }
        if (eruptProp.isKeepUploadFileName()) {
            return File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE) + File.separator + file.getOriginalFilename()
                    .replaceAll("&|#|\\?|\\s", "");
        } else {
            String[] fileNameSplit = file.getOriginalFilename().split("\\.");
            return File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE)
                    + File.separator + RandomStringUtils.randomAlphabetic(12) + EruptConst.DOT + fileNameSplit[fileNameSplit.length - 1];
        }
    }

    public String upload(MultipartFile file, String path) throws Exception {
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
                        throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.upload_error.cannot_created")+ ": " + dest.getParentFile().getAbsolutePath());
                    }
                }
                file.transferTo(dest);
            }
            return path.replace("\\", "/");
        } catch (Exception e) {
            log.error("erupt upload error", e);
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("erupt.upload_error") + " " + e.getMessage());
        }
    }

}
