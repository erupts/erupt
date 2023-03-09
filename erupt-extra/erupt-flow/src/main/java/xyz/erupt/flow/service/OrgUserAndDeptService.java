package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.vo.OrgTreeVo;

import java.util.List;

/**
 * @author : willian fu
 * @version : 1.0
 */
public interface OrgUserAndDeptService {

    /**
     * 查询组织架构树
     * @param deptId 部门id
     * @param isDept 只查询部门架构
     * @param showLeave 是否显示离职员工
     * @return 组织架构树数据
     */
    List<OrgTreeVo> getOrgTreeData(Integer deptId, Boolean isDept, Boolean showLeave);

    /**
     * 模糊搜索用户
     * @param userName 用户名/拼音/首字母
     * @return 匹配到的用户
     */
    List<OrgTreeVo> getOrgTreeUser(String userName);

}
