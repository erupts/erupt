package xyz.erupt.eruptlimit.repository;

import xyz.erupt.eruptlimit.model.EruptUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by liyuepeng on 2018-12-13.
 */
public interface UserRepository extends JpaRepository<EruptUser, Long> {

    EruptUser findByAccount(String account);

//    @Query("FROM EruptUser E WHERE E.account = :account AND E.password = :pwd AND ")
//    EruptUser findUser(@Param("account") String account, @Param("pwd") String pwd);

}
