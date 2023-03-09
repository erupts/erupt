package xyz.erupt.flow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.FormsService;

import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class FormsController {

    @Autowired
    private FormsService formsService;

    /**
     * 创建新的表单
     * @param form
     * @return
     */
    @PostMapping("/admin/form")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel createForm(@RequestBody OaForms form){
        formsService.createForm(form);
        return EruptApiModel.successApi();
    }

    /**
     * 查询表单模板数据
     * @param formId 模板id
     * @return 模板详情数据
     */
    @GetMapping("/admin/form/detail/{formId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public EruptApiModel getFormById(@PathVariable String formId){
        return EruptApiModel.successApi(formsService.getById(formId));
    }

    /**
     * 修改表单信息
     * @param formId 摸板ID
     * @return 操作结果
     */
    @PutMapping("/admin/form/{formId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel updateForm(@PathVariable Long formId, @RequestBody OaForms oaForms){
        oaForms.setFormId(formId);
        formsService.updateById(oaForms);
        return EruptApiModel.successApi();
    }

    /**
     * 删除流程
     * @param formId 摸板ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/form/{formId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel removeForm(@PathVariable Long formId){
        formsService.removeById(formId);
        return EruptApiModel.successApi();
    }

    /**
     * 编辑表单详情
     * @param template 表单模板信息
     * @return 修改结果
     */
    @PutMapping("/admin/form/detail")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel updateFormDetail(@RequestBody OaForms template){
        formsService.updateFormDetail(template);
        return EruptApiModel.successApi();
    }

    /**
     * 表单排序
     * @param formIds 表单ID
     * @return 排序结果
     */
    @PutMapping("/admin/form/sort")
    @EruptRouter(verifyType = EruptRouter.VerifyType.MENU)
    public EruptApiModel formsSort(@RequestBody List<Long> formIds){
        formsService.formsSort(formIds);
        return EruptApiModel.successApi();
    }
}
