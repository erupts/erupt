package xyz.erupt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.auth.model.EruptUser;

/**
 * @author liyuepeng
 * @date 2018-12-13.
 */
public interface UserRepository extends JpaRepository<EruptUser, Long> {

    /**
     * 通过用户名获取用户数据
     *
     * @param account 用户名
     * @return 用户类
     */
    EruptUser findByAccount(String account);

}
