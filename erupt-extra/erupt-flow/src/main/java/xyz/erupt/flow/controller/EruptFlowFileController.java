package xyz.erupt.flow.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.DateUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.view.R;
import xyz.erupt.flow.bean.vo.FileUploadResult;
import xyz.erupt.flow.constant.FlowConstant;

import java.io.File;
import java.util.Date;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
@RequiredArgsConstructor
@Slf4j
public class EruptFlowFileController {

    private final EruptProp eruptProp;

    /**
     * 上传图片
     *
     * @param file 文件
     */
    @SneakyThrows
    @PostMapping("/upload")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<FileUploadResult> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || StringUtils.isBlank(file.getOriginalFilename())) {
            return R.error("上传失败，请选择文件");
        }
        String path;
        if (eruptProp.isKeepUploadFileName()) {
            path = File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE) + File.separator + file.getOriginalFilename()
                    .replaceAll("&|#|\\?|\\s", "");
        } else {
            String[] fileNameSplit = file.getOriginalFilename().split("\\.");
            path = File.separator + DateUtil.getFormatDate(new Date(), DateUtil.DATE)
                    + File.separator + RandomStringUtils.randomAlphabetic(12) + EruptConst.DOT + fileNameSplit[fileNameSplit.length - 1];
        }
        //加一个独享空间，方便查找
        path = File.separator + FlowConstant.SERVER_NAME + path;

        try {
            //是否本地存储
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
                        return R.error("上传失败，文件目录无法创建");
                    }
                }
                file.transferTo(dest);
            }
            return R.ok(new FileUploadResult(file.getOriginalFilename(), EruptRestPath.ERUPT_ATTACHMENT + path.replace("\\", "/")));
        } catch (Exception e) {
            return R.error("上传失败，" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param path 路径
     */
    @SneakyThrows
    @DeleteMapping("/file")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> deleteFile(@RequestParam("path") String path) {
        path = eruptProp.getUploadPath() + path.replace(EruptRestPath.ERUPT_ATTACHMENT, "");
        File file = new File(path);
        if (file.exists()) {
            FileUtils.delete(file);
        }
        return R.ok();
    }

}

