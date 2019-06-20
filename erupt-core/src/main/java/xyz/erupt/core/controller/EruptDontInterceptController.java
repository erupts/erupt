package xyz.erupt.core.controller;

import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liyuepeng on 2019-03-15.
 */
@RestController
@RequestMapping(RestPath.DONT_INTERCEPT)
public class EruptDontInterceptController {

    @Value("${erupt.uploadPath:/opt/file}")
    private String uploadPath;

    @RequestMapping(value = "/preview-attachment", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ResponseBody
    @EruptRouter(loginVerify = false)
    public byte[] preview(@RequestParam("path") String path) {
        if (!path.startsWith(File.separator)) {
            path = File.separator + path;
        }
        File file = new File(uploadPath + path);
        try {
            @Cleanup InputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, inputStream.available());
            inputStream.close();
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }


}
