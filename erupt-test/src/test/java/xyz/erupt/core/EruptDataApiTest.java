package xyz.erupt.core;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import xyz.erupt.EruptApplicationTests;

@Service
@Rollback
@Transactional
public class EruptDataApiTest extends EruptApplicationTests {

    @Test
    public void query() {

    }

}
