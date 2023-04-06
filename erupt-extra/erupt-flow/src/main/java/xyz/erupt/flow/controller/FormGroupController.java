package xyz.erupt.flow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaFormGroups;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.FormGroupService;

import java.util.List;

/**
 * @author : zhu
 * @date : 2023/02/10
 */
@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class FormGroupController {

    @Autowired
    private FormGroupService formGroupService;

    /**
     * 查询所有表单分组
     * @return
     */
    @GetMapping("/admin/form/group")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getFormGroups(String keywords){
        List<OaFormGroups> formGroups = formGroupService.getFormGroups(OaFormGroups.builder()
                .keywords(keywords)
                .build());
        return EruptApiModel.successApi(formGroups);
    }

    /**
     * 查询所有表单分组
     * @return
     */
    @GetMapping("/admin/form/group/list")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getFormGroupList(){
        List<OaFormGroups> formGroupList = formGroupService.getFormGroupList();
        return EruptApiModel.successApi(formGroupList);
    }

    /**
     * 表单分组排序
     * @param groups 分组数据
     * @return 排序结果
     */
    @PutMapping("/admin/form/group/sort")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel formGroupsSort(@RequestBody List<Long> groups){
        formGroupService.formGroupsSort(groups);
        return EruptApiModel.successApi();
    }

    /**
     * 修改分组
     * @param id 分组ID
     * @param name 分组名
     * @return 修改结果
     */
    @PutMapping("/admin/form/group")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel updateFormGroupName(@RequestParam Long id,
                                       @RequestParam String name){
        formGroupService.updateFormGroupName(id, name);
        return EruptApiModel.successApi();
    }

    /**
     * 新增表单分组
     * @param groupName 分组名
     * @return 添加结果
     */
    @PostMapping("/admin/form/group")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel createFormGroup(@RequestParam String groupName){
        formGroupService.createFormGroup(groupName);
        return EruptApiModel.successApi();
    }

    /**
     * 删除分组
     * @param groupId 分组ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/form/group/{groupId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel deleteFormGroup(@PathVariable Long groupId){
        formGroupService.deleteFormGroup(groupId);
        return EruptApiModel.successApi();
    }
}
