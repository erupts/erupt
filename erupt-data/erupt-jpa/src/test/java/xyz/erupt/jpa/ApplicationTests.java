package xyz.erupt.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.entity.Class;
import xyz.erupt.jpa.entity.Student;

import javax.annotation.Resource;

@SpringBootTest
class ApplicationTests {

    @Resource
    private EruptDao eruptDao;

    @Test
    void lambdaQueryTest() {
        eruptDao.lambdaQuery(Student.class).eq(Student::getName, "张三");
        eruptDao.lambdaQuery(Student.class).with(Student::getClazz)
                .eq(Class::getName, "一班")
                .ne(Class::getName, "一班")
                .le(Class::getName, "一班")
                .lt(Class::getName, "一班")
                .ge(Class::getName, "一班")
                .gt(Class::getName, "一班")
                .in(Class::getName, "一班")
                .notIn(Class::getName, "一班")
                .isNull(Class::getName)
                .isNotNull(Class::getName)
                .between(Class::getName, "一班", "二班")
                .like(Class::getName, "二班")
                .likeValue(Class::getName, "二班%")
                .orderBy(Class::getName)
                .with();

        eruptDao.lambdaQuery(Student.class)
                .eq(Student::getName, "一班")
                .ne(Student::getName, "一班")
                .le(Student::getName, "一班")
                .lt(Student::getName, "一班")
                .ge(Student::getName, "一班")
                .gt(Student::getName, "一班")
                .in(Student::getName, "一班")
                .isNull(Student::getName)
                .isNotNull(Student::getName)
                .between(Student::getName, "一班", "二班")
                .like(Student::getName, "二班")
                .likeValue(Student::getName, "二班%")
                .orderBy(Student::getName)
                .with();

        eruptDao.lambdaQuery(Student.class)
                .eq(true, Student::getName, "一班")
                .ne(true, Student::getName, "一班")
                .le(true, Student::getName, "一班")
                .lt(true, Student::getName, "一班")
                .ge(true, Student::getName, "一班")
                .gt(true, Student::getName, "一班")
                .in(true, Student::getName, "一班")
                .isNull(true, Student::getName)
                .isNotNull(true, Student::getName)
                .between(true, Student::getName, "一班", "二班")
                .like(true, Student::getName, "二班")
                .likeValue(true, Student::getName, "二班%")
                .orderBy(true, Student::getName)
                .with();
    }

}
