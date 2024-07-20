package xyz.erupt.flow.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
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
    public R<List<OrgTreeVo>> getOrgTree(Long deptId, String keyword) {
        return R.ok(userLinkService.getOrgTree(deptId, keyword));
    }

    /**
     * 查询用户
     */
    @GetMapping("/oa/org/tree/user")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OrgTreeVo>> getOrgUserTree(Long deptId, String keyword) {
        return R.ok(userLinkService.getOrgTreeUser(deptId, keyword));
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/oa/role")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<List<OrgTreeVo>> getRoleList(String keyword) {
        return R.ok(userLinkService.getRoleList(keyword));
    }

}
