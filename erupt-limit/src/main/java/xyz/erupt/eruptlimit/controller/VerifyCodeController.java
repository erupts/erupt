package xyz.erupt.eruptlimit.controller;

/**
 * Created by liyuepeng on 2018-12-14.
 * 验证码
 */

import xyz.erupt.eruptcache.redis.RedisService;
import xyz.erupt.eruptlimit.constant.RedisKey;
import xyz.erupt.util.IdentifyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/verify")
public class VerifyCodeController {

    @Autowired
    private RedisService redisService;

    /**
     * 生成验证码
     */
    @GetMapping
    @RequestMapping("/code-img")
    public void createCode(HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
//        redisService.put("");
        Cookie[] cookies = request.getCookies();
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        // 禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 自定义宽、高、字数和干扰线的条数
        IdentifyCode code = new IdentifyCode(100, 38, 4, 10);
        // 存入session
        HttpSession session = request.getSession();
        System.out.println(session.getAttribute(RedisKey.VERIFY_CODE));
        session.setAttribute(RedisKey.VERIFY_CODE, code.getCode());
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
