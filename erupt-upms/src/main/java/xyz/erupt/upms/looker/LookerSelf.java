package xyz.erupt.upms.looker;

import org.springframework.stereotype.Service;
import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
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
@PreDataProxy(LookerSelf.class)
@Service
public class LookerSelf extends HyperModel implements DataProxy<Object> {

    @Resource
    @Transient
    private HttpServletRequest request;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Override
    public String beforeFetch() {
        if (!eruptUserService.getCurrentEruptUser().getIsAdmin()) {
            return request.getHeader("erupt") + ".createUser.id = " + eruptUserService.getCurrentUid();
        } else {
            return null;
        }
    }
}
