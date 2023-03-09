package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaForms;

import java.util.List;

public interface FormsService extends IService<OaForms> {

    void updateFormDetail(OaForms forms);

    void createForm(OaForms form);

    List<OaForms> listByGroupId(Long groupId, String keywords);

    void formsSort(List<Long> formIds);
}
