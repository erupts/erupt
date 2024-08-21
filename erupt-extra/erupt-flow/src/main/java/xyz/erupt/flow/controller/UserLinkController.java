package xyz.erupt.flow.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;

import java.util.List;

/**
 * 用户体系的接口，部门，用户，角色相关的全在这里
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class UserLinkController {

    @Autowired
    private UserLinkServiceHolder userLinkService;

    /**
     * 查询组织架构树
     */
    @GetMapping("/oa/org/tree")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getOrgTree(String deptId, String keyword){
        List<OrgTreeVo> orgTreeData = userLinkService.getOrgTree(deptId, keyword);
        return EruptApiModel.successApi(orgTreeData);
    }

    /**
     * 查询用户
     */
    @GetMapping("/oa/org/tree/user")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getOrgUserTree(String deptId, String keyword){
        List<OrgTreeVo> orgTreeUser = userLinkService.getOrgTreeUser(deptId, keyword);
        return EruptApiModel.successApi(orgTreeUser);
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/oa/role")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getRoleList(String keyword){
        List<OrgTreeVo> orgTreeData = userLinkService.getRoleList(keyword);
        return EruptApiModel.successApi(orgTreeData);
    }
}
