package xyz.erupt.auth.controller;

/**
 * Created by liyuepeng on 2018-12-14.
 * 验证码
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.auth.constant.SessionKey;
import xyz.erupt.auth.util.IdentifyCode;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.service.SessionService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@RestController
public class VerifyCodeController {

    @Autowired
    private SessionService sessionService;

    /**
     * 生成验证码
     */
    @GetMapping
    @RequestMapping(RestPath.ERUPT_API + "/code-img")
    public void createCode(@RequestParam("account") String account, HttpServletResponse response) throws Exception {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        // 禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 自定义宽、高、字数和干扰线的条数
        IdentifyCode code = new IdentifyCode(100, 38, 4, 20);
        // 验证码过期时间1分钟
        sessionService.put(SessionKey.VERIFY_CODE + account, code.getCode(), 1);
        // 响应图片
        ServletOutputStream out = response.getOutputStream();
        code.write(out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }


}
