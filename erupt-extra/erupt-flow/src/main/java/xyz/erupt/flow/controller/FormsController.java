package xyz.erupt.flow.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.EruptRestPath;
import xyz.erupt.core.view.R;
import xyz.erupt.flow.bean.entity.OaForms;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.service.FormsService;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(EruptRestPath.ERUPT_API + "/" + FlowConstant.SERVER_NAME)
public class FormsController {

    @Resource
    private FormsService formsService;

    @Resource
    private EruptDao eruptDao;

    /**
     * 创建新的表单
     *
     * @param form
     */
    @PostMapping("/admin/form")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> createForm(@RequestBody OaForms form) {
        formsService.createForm(form);
        return R.ok();
    }

    /**
     * 查询表单模板数据
     *
     * @param id 模板id
     * @return 模板详情数据
     */
    @GetMapping("/admin/form/detail/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<OaForms> getFormById(@PathVariable Long id) {
        return R.ok(eruptDao.findById(OaForms.class, id));
    }

    /**
     * 修改表单信息
     *
     * @param id 摸板ID
     * @return 操作结果
     */
    @PutMapping("/admin/form/{id}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> updateForm(@PathVariable Long id, @RequestBody OaForms oaForms) {
        oaForms.setFormId(id);
        formsService.updateById(oaForms);
        return R.ok();
    }

    /**
     * 删除流程
     *
     * @param formId 摸板ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/form/{formId}")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    @Transactional
    public R<Void> removeForm(@PathVariable Long formId) {
        eruptDao.delete(OaForms.builder().formId(formId).build());
        return R.ok();
    }

    /**
     * 编辑表单详情
     *
     * @param template 表单模板信息
     * @return 修改结果
     */
    @PutMapping("/admin/form/detail")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> updateFormDetail(@RequestBody OaForms template) {
        formsService.updateFormDetail(template);
        return R.ok();
    }

    /**
     * 表单排序
     *
     * @param formIds 表单ID
     * @return 排序结果
     */
    @PutMapping("/admin/form/sort")
    @EruptRouter(verifyType = EruptRouter.VerifyType.LOGIN)
    public R<Void> formsSort(@RequestBody List<Long> formIds) {
        formsService.formsSort(formIds);
        return R.ok();
    }
}
