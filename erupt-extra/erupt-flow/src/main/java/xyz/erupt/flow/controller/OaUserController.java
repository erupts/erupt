package xyz.erupt.flow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.OrgUserAndDeptService;

import java.util.List;

/**
 * @author : zhu
 * @date : 2023/02/10
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class OaUserController {

    @Autowired
    private OrgUserAndDeptService orgService;

    /**
     * 查询组织架构树
     * @param deptId 部门id
     * @param isDept 只查询部门架构
     * @param showLeave 是否显示离职员工
     * @return 组织架构树数据
     */
    @GetMapping("/oa/org/tree")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel getOrgTreeData(@RequestParam(defaultValue = "0") Integer deptId,
                                        @RequestParam(defaultValue = "false") Boolean isDept,
                                        @RequestParam(defaultValue = "false") Boolean showLeave){
        List<OrgTreeVo> orgTreeData = orgService.getOrgTreeData(deptId, isDept, showLeave);
        return EruptApiModel.successApi(orgTreeData);
    }

    /**
     * 模糊搜索用户
     * @param userName 用户名/拼音/首字母
     * @return 匹配到的用户
     */
    @GetMapping("/oa/org/tree/user/search")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel getOrgTreeUser(@RequestParam String userName){
        List<OrgTreeVo> orgTreeUser = orgService.getOrgTreeUser(userName.trim());
        return EruptApiModel.successApi(orgTreeUser);
    }


}
