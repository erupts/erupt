package xyz.erupt.upms.looker;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

/**
 * @author liyuepeng
 * @date 2021/3/10 11:30
 */
@MappedSuperclass
@PreDataProxy(LookerPostLevel.class)
@Service
public class LookerPostLevel extends HyperModel implements DataProxy<Object> {

    @Resource
    @Transient
    private HttpServletRequest request;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Override
    public String beforeFetch() {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (!eruptUser.getIsAdmin()) {
            if (null == eruptUser.getEruptOrg() || null == eruptUser.getEruptPost()) {
                throw new EruptWebApiRuntimeException(eruptUser.getName() + " unbounded department cannot filter data");
            }
            return request.getHeader("erupt") + ".createUser.eruptOrg.id = " + eruptUser.getEruptOrg().getId()
                    + " and " + request.getHeader("erupt") + ".createUser.eruptPost.weight <=" + eruptUser.getEruptPost().getWeight();
        } else {
            return null;
        }
    }
}
