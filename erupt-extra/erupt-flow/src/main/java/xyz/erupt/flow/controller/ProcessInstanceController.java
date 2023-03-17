package xyz.erupt.flow.controller;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.ProcessInstanceService;
import xyz.erupt.flow.web.EruptApiPageModel;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @GetMapping("/data/OaProcessInstance/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getById(@PathVariable("id") Long id) {
        OaProcessInstance byId = processInstanceService.getById(id);
        return EruptApiModel.successApi(byId);
    }

    /**
     * 查询与我相关的流程
     * @return
     */
    @GetMapping("/inst/mine/about")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getMineAbout(String keywords, int pageIndex, int pageSize) {
        PageHelper.startPage(pageIndex, pageSize);//分页
        List<OaProcessInstanceHistory> datas = processInstanceService.getMineAbout(keywords, pageIndex, pageSize);
        return EruptApiPageModel.successApi(datas, pageIndex, pageIndex);
    }
}
