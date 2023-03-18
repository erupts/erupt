package xyz.erupt.flow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.erupt.flow.bean.entity.OaTask;

import java.util.List;

public interface OaTaskRepository extends JpaRepository<OaTask, Long> {

    @Query("from OaTask where processInstId=:instId and active=true and activityKey='root' order by completeSort asc")
    OaTask getStartTaskByInst(Long instId);

    @Query("from OaTask where (1=2" +
            " or (taskOwner=:userName)" +
            " or (taskOwner is null and assignee=:userName)" +
            " or (taskOwner is null and assignee is null and id in" +
            "      (select taskId from OaTaskUserLink where 1=2" +
            "        or (userLinkType='ROLES' and roleId in " +
            "             (select code from EruptRole where id in" +
            "               (" +
            "                 select roleId from EruptUserRole where userId in (select id from EruptUser where account=:userName)" +
            "               )" +
            "             )" +
            "           )" +
            "        or (userLinkType='USERS' and userId=:userName)" +
            "      )" +
            "    )" +
            ") and (1=1" +
            " or taskName like :keywords" +
            " or taskName like :keywords" +
            " or taskName like :keywords" +
            " or taskName like :keywords" +
            ")")
    List<OaTask> listMyTasks(String userName, String keywords);

    @Query("from OaTask where (1=2" +
            " or (taskOwner=:userName)" +
            " or (taskOwner is null and assignee=:userName)" +
            " or (taskOwner is null and assignee is null and id in" +
            "      (select taskId from OaTaskUserLink where 1=2" +
            "        or (userLinkType='ROLES' and roleId in " +
            "             (select code from EruptRole where id in" +
            "               (" +
            "                 select roleId from EruptUserRole where userId in (select id from EruptUser where account=:userName)" +
            "               )" +
            "             )" +
            "           )" +
            "        or (userLinkType='USERS' and userId=:userName)" +
            "      )" +
            "    )" +
            ")")
    List<OaTask> listMyTasks(String userName);

    void deleteAllByProcessInstId(Long procInstId);

    List<OaTask> findByActivityIdAndActiveOrderByCompleteSortAsc(Long activityId, boolean actived);
}
