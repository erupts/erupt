package xyz.erupt.jpa.dao;

import org.junit.jupiter.api.Test;
import xyz.erupt.jpa.ApplicationTests;
import xyz.erupt.jpa.entity.Class;
import xyz.erupt.jpa.entity.Student;

import javax.annotation.Resource;

public class EruptLambdaQueryTest extends ApplicationTests {

    @Resource
    private EruptDao eruptDao;

    @Test
    void lambdaQueryTest2() {
        System.out.println(1);
    }

    @Test
    void lambdaQueryTest() {
        eruptDao.lambdaQuery(Student.class).eq(Student::getName, "张三");
        eruptDao.lambdaQuery(Student.class).with(Student::getClazz)
                .eq(xyz.erupt.jpa.entity.Class::getName, "一班")
                .ne(xyz.erupt.jpa.entity.Class::getName, "一班")
                .le(xyz.erupt.jpa.entity.Class::getName, "一班")
                .lt(xyz.erupt.jpa.entity.Class::getName, "一班")
                .ge(xyz.erupt.jpa.entity.Class::getName, "一班")
                .gt(xyz.erupt.jpa.entity.Class::getName, "一班")
                .in(xyz.erupt.jpa.entity.Class::getName, "一班")
                .notIn(xyz.erupt.jpa.entity.Class::getName, "一班")
                .isNull(xyz.erupt.jpa.entity.Class::getName)
                .isNotNull(xyz.erupt.jpa.entity.Class::getName)
                .between(xyz.erupt.jpa.entity.Class::getName, "一班", "二班")
                .like(xyz.erupt.jpa.entity.Class::getName, "二班")
                .likeValue(xyz.erupt.jpa.entity.Class::getName, "二班%")
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
