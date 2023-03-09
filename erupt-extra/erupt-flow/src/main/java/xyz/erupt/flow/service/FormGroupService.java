package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaFormGroups;

import java.util.List;

/**
 * @author : willian fu
 * @date : 2022/7/4
 */
public interface FormGroupService extends IService<OaFormGroups> {

    /**
     * 查询表单组
     *
     * @return 表单组数据
     */
    List<OaFormGroups> getFormGroups(OaFormGroups vo);

    /**
     * 表单及分组排序
     *
     * @param groups 分组数据
     * @return 排序结果
     */
    void formGroupsSort(List<Long> groups);

    /**
     * 修改分组
     *
     * @param id   分组ID
     * @param name 分组名
     * @return 修改结果
     */
    void updateFormGroupName(Long id, String name);

    /**
     * 新增表单分组
     *
     * @param name 分组名
     * @return 添加结果
     */
    void createFormGroup(String name);

    /**
     * 删除分组
     *
     * @param id 分组ID
     * @return 删除结果
     */
    void deleteFormGroup(Long id);

    List<OaFormGroups> getFormGroupList();
}
