package xyz.erupt.flow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.mapper.OaDepartmentsMapper;
import xyz.erupt.flow.mapper.OaUsersMapper;
import xyz.erupt.flow.service.OrgUserAndDeptService;

import java.util.LinkedList;
import java.util.List;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Service
public class OrgUserAndDeptServiceImpl implements OrgUserAndDeptService {

    @Autowired
    private OaDepartmentsMapper departsMapper;

    @Autowired
    private OaUsersMapper usersMapper;

    /**
     * 查询组织架构树
     *
     * @param deptId    部门id
     * @param isDept    只查询部门架构
     * @param showLeave 是否显示离职员工
     * @return 组织架构树数据
     */
    @Override
    public List<OrgTreeVo> getOrgTreeData(Integer deptId, Boolean isDept, Boolean showLeave) {
        //查询所有部门及员工
        List<OrgTreeVo> orgs = new LinkedList<>();
        orgs.addAll(departsMapper.selectByDept(deptId));
        if (!isDept){
            usersMapper.selectUsersByDept(deptId).forEach(us -> orgs.add(us));
        }
        return orgs;
    }

    /**
     * 模糊搜索用户
     *
     * @param userName 用户名/拼音/首字母
     * @return 匹配到的用户
     */
    @Override
    public List<OrgTreeVo> getOrgTreeUser(String userName) {
         return usersMapper.selectUsersByPy(userName);
    }
}
