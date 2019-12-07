package xyz.erupt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.auth.model.EruptUser;

/**
 * Created by liyuepeng on 2018-12-13.
 */
public interface UserRepository extends JpaRepository<EruptUser, Long> {

    EruptUser findByAccount(String account);

}
