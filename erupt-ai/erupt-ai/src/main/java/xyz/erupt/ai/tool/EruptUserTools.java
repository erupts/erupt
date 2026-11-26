package xyz.erupt.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.ai.AiToolbox;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.context.MetaContext;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.EruptUserVo;
import xyz.erupt.upms.service.EruptMenuService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AiToolbox
@Component
public class EruptUserTools {

    private static final int MAX_USERS = 50;

    private record UserBrief(Long id, String name) {
    }

    @Resource
    private EruptDao eruptDao;

    @Resource
    private EruptMenuService eruptMenuService;

    @Tool("Ask the current system logged-in user")
    public String eruptUserInfo() {
        EruptUser eruptUser = eruptDao.find(EruptUser.class, MetaContext.getUser().getUid());
        eruptUser.setRoles(null);
        eruptUser.setPassword(null);
        return GsonFactory.getGson().toJson(eruptUser);
    }

    @Tool("Get current user's roles and accessible menu permissions")
    public String eruptUserPermissions() {
        EruptUser eruptUser = eruptDao.find(EruptUser.class, MetaContext.getUser().getUid());
        List<EruptMenu> menus = eruptMenuService.getUserAllMenu(eruptUser);

        List<Map<String, String>> roleList = Optional.ofNullable(eruptUser.getRoles())
                .orElse(Collections.emptySet()).stream()
                .map(role -> {
                    Map<String, String> r = new LinkedHashMap<>();
                    r.put("name", role.getName());
                    r.put("code", role.getCode());
                    return r;
                }).collect(Collectors.toList());

        List<Map<String, String>> menuList = menus.stream()
                .map(menu -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("name", menu.getName());
                    m.put("code", menu.getCode());
                    m.put("type", menu.getType());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isAdmin", eruptUser.getIsAdmin());
        result.put("roles", roleList);
        result.put("menus", menuList);
        return GsonFactory.getGson().toJson(result);
    }

    @Tool("Find enabled users by name, returning id and name. Use it before any tool that takes user ids.")
    public String findUsers(@P(value = "Part of the user's name; omit to list the first users", required = false) String keyword) {
        return GsonFactory.getGson().toJson(eruptDao.lambdaQuery(EruptUserVo.class)
                .eq(EruptUserVo::getStatus, true)
                .like(StringUtils.isNotBlank(keyword), EruptUserVo::getName, keyword)
                .orderByAsc(EruptUserVo::getName).limit(MAX_USERS)
                .list().stream().map(it -> new UserBrief(it.getId(), it.getName())).toList());
    }

    @Tool("Get current date and time of the server with timezone information.")
    public String getCurrentDateTime() {
        ZonedDateTime now = ZonedDateTime.now();
        return "DateTime: " + now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                + "\nTimezone: " + now.getZone()
                + "\nTimestamp: " + now.toInstant().toEpochMilli();
    }

}
