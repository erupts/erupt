package xyz.erupt.core.service;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.EruptApiModel;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author YuePeng
 * date 2023/9/2 23:26
 */
public class EruptFileService {

    @Resource
    private EruptProp eruptProp;

    public EruptApiModel upload(@RequestParam("file") MultipartFile file, String path) {
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

}
