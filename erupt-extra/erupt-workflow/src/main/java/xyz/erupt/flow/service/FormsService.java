package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaForms;

import java.io.Serializable;
import java.util.List;

public interface FormsService {

    void updateFormDetail(OaForms forms);

    void createForm(OaForms form);

    List<OaForms> listByGroupId(Long groupId, String keywords);

    void formsSort(List<Long> formIds);

    void updateById(OaForms entity);

    void removeById(Serializable id);
}
