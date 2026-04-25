package xyz.erupt.test.core;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.query.Grade;
import xyz.erupt.test.model.query.Student;

@Rollback
@Transactional
public class LambdaQueryTest extends EruptApplicationTests {


    @Test
    void lambdaQueryTest() {
        eruptDao.lambdaQuery(Student.class).eq(Student::getName, "John");
        eruptDao.lambdaQuery(Student.class).with(Student::getGrade)
                .eq(Grade::getName, "A")
                .ne(Grade::getName, "A")
                .le(Grade::getName, "A")
                .lt(Grade::getName, "A")
                .ge(Grade::getName, "A")
                .gt(Grade::getName, "A")
                .in(Grade::getName, "A")
                .notIn(Grade::getName, "A")
                .isNull(Grade::getName)
                .isNotNull(Grade::getName)
                .between(Grade::getName, "A", "B")
                .like(Grade::getName, "B")
                .likeValue(Grade::getName, "B%")
                .orderBy(Grade::getName)
                .with().list();

        eruptDao.lambdaQuery(Student.class)
                .eq(Student::getName, "A")
                .ne(Student::getName, "A")
                .le(Student::getName, "A")
                .lt(Student::getName, "A")
                .ge(Student::getName, "A")
                .gt(Student::getName, "A")
                .in(Student::getName, "A")
                .isNull(Student::getName)
                .isNotNull(Student::getName)
                .between(Student::getName, "A", "B")
                .like(Student::getName, "B")
                .likeValue(Student::getName, "B%")
                .orderBy(Student::getName)
                .with().list();

        eruptDao.lambdaQuery(Student.class)
                .eq(true, Student::getName, "A")
                .ne(true, Student::getName, "A")
                .le(true, Student::getName, "A")
                .lt(true, Student::getName, "A")
                .ge(true, Student::getName, "A")
                .gt(true, Student::getName, "A")
                .in(true, Student::getName, "A")
                .isNull(true, Student::getName)
                .isNotNull(true, Student::getName)
                .between(true, Student::getName, "A", "B")
                .like(true, Student::getName, "B")
                .likeValue(true, Student::getName, "B%")
                .orderBy(true, Student::getName)
                .with().list();
    }

}
