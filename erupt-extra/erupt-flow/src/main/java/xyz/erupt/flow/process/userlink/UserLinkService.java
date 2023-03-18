package xyz.erupt.flow.process.userlink;

import xyz.erupt.flow.bean.vo.OrgTreeVo;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 流程中的部门，用户，角色接口
 * 如需要自己的用户体系，实现此接口即可
 */
public interface UserLinkService extends Comparable<UserLinkService> {

    /**
     * 实现类的优先级
     * 默认给个1，比 DefaultUserLinkServiceImpl 大
     * @return
     */
    default int priority() {
        return 1;
    }

    @Override
    default int compareTo(UserLinkService to)  {
        //比较优先级
        return this.priority() - to.priority();
    }

    /**
     * 查询组织架构树
     */
    List<OrgTreeVo> getOrgTree(String parentId, String keyword);

    /**
     * 模糊搜索用户
     */
    List<OrgTreeVo> getOrgTreeUser(String deptId, String keyword);

    /**
     * 查询角色列表
     */
    List<OrgTreeVo> getRoleList(String keyword);

    /**
     * 查询指定用户的所有主管
     * @param userId 当前用户id
     * @param startLevel 从多少级主管开始查，小于1则取1
     * @param limitLevel 最多查询到多少级主管，-1表示不限级
     * @return key=主管的级别，value=该级主管列表
     */
    LinkedHashMap<Integer, List<OrgTreeVo>> getLeaderMap(String userId, int startLevel, int limitLevel);

    /**
     * 根据角色列表查询用户id列表
     */
    LinkedHashSet<OrgTreeVo> getUserIdsByRoleIds(String... roleIds);

    /**
     * 查询超管用户列表
     * @return
     */
    LinkedHashSet<OrgTreeVo> getAdminUsers();

    /**
     * 根据用户查询他的角色列表
     * @param userName
     * @return
     */
    LinkedHashSet<String> getRoleIdsByUserId(String userName);
}
