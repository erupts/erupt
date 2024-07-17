package xyz.erupt.flow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.ProcessInstanceService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class ProcessInstanceController {

    @Resource
    private ProcessInstanceService processInstanceService;

    @Resource
    private EruptDao eruptDao;

    @GetMapping("/data/OaProcessInstance/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<OaProcessInstance> getById(@PathVariable("id") Long id) {
        return R.ok(eruptDao.findById(OaProcessInstance.class, id));
    }

    /**
     * 查询与我相关的流程
     *
     * @return
     */
    @GetMapping("/inst/mine/about")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OaProcessInstanceHistory>> getMineAbout(String keywords, int pageIndex, int pageSize) {
        return R.ok(processInstanceService.getMineAbout(keywords, pageIndex, pageSize));
    }
}
