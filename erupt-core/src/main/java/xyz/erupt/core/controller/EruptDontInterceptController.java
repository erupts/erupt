package xyz.erupt.core.controller;

import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.constant.RestPath;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by liyuepeng on 2019-03-15.
 */
@RestController
@RequestMapping(RestPath.DONT_INTERCEPT)
public class EruptDontInterceptController {

    @Value("${erupt.uploadPath:/opt/file}")
    private String uploadPath;

    @RequestMapping(value = "/preview-attachment", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ResponseBody
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
