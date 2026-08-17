package xyz.erupt.ai_staff.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.ai_staff.channel.ChannelRequest;
import xyz.erupt.ai_staff.service.AiStaffChannelService;
import xyz.erupt.core.constant.EruptRestPath;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Open callback endpoint for IM platforms. No erupt session auth here —
 * each platform authenticates through its own signature, verified by the
 * channel implementation, plus the unguessable channel code in the path.
 *
 * @author YuePeng
 * date 2026/8/3
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/ai-staff/channel")
public class AiStaffChannelController {

    @Resource
    private AiStaffChannelService aiStaffChannelService;

    @RequestMapping(value = "/{code}", method = {RequestMethod.GET, RequestMethod.POST})
    public String callback(@PathVariable("code") String code, HttpServletRequest request,
                           @RequestBody(required = false) String body) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name ->
                headers.put(name.toLowerCase(Locale.ROOT), request.getHeader(name)));
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values.length > 0 ? values[0] : null));
        return aiStaffChannelService.onCallback(code, ChannelRequest.builder()
                .method(request.getMethod())
                .headers(headers)
                .params(params)
                .body(body)
                .build());
    }

}
