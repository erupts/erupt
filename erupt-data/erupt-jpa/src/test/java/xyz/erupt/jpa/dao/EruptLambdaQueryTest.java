//package xyz.erupt.jpa.dao;
//
//import org.junit.jupiter.api.Test;
//import xyz.erupt.jpa.ApplicationTests;
//import xyz.erupt.jpa.entity.Class;
//import xyz.erupt.jpa.entity.Student;
//
//import jakarta.annotation.Resource;
//
//public class EruptLambdaQueryTest extends ApplicationTests {
//
//    @Resource
//    private EruptDao eruptDao;
//
//    @Test
//    void lambdaQueryTest() {
//        eruptDao.lambdaQuery(Student.class).eq(Student::getName, "John");
//        eruptDao.lambdaQuery(Student.class).with(Student::getClazz)
//                .eq(xyz.erupt.jpa.entity.Class::getName, "A")
//                .ne(xyz.erupt.jpa.entity.Class::getName, "A")
//                .le(xyz.erupt.jpa.entity.Class::getName, "A")
//                .lt(xyz.erupt.jpa.entity.Class::getName, "A")
//                .ge(xyz.erupt.jpa.entity.Class::getName, "A")
//                .gt(xyz.erupt.jpa.entity.Class::getName, "A")
//                .in(xyz.erupt.jpa.entity.Class::getName, "A")
//                .notIn(xyz.erupt.jpa.entity.Class::getName, "A")
//                .isNull(xyz.erupt.jpa.entity.Class::getName)
//                .isNotNull(xyz.erupt.jpa.entity.Class::getName)
//                .between(xyz.erupt.jpa.entity.Class::getName, "A", "B")
//                .like(xyz.erupt.jpa.entity.Class::getName, "B")
//                .likeValue(xyz.erupt.jpa.entity.Class::getName, "B%")
//                .orderBy(Class::getName)
//                .with();
//
//        eruptDao.lambdaQuery(Student.class)
//                .eq(Student::getName, "A")
//                .ne(Student::getName, "A")
//                .le(Student::getName, "A")
//                .lt(Student::getName, "A")
//                .ge(Student::getName, "A")
//                .gt(Student::getName, "A")
//                .in(Student::getName, "A")
//                .isNull(Student::getName)
//                .isNotNull(Student::getName)
//                .between(Student::getName, "A", "B")
//                .like(Student::getName, "B")
//                .likeValue(Student::getName, "B%")
//                .orderBy(Student::getName)
//                .with();
//
//        eruptDao.lambdaQuery(Student.class)
//                .eq(true, Student::getName, "A")
//                .ne(true, Student::getName, "A")
//                .le(true, Student::getName, "A")
//                .lt(true, Student::getName, "A")
//                .ge(true, Student::getName, "A")
//                .gt(true, Student::getName, "A")
//                .in(true, Student::getName, "A")
//                .isNull(true, Student::getName)
//                .isNotNull(true, Student::getName)
//                .between(true, Student::getName, "A", "B")
//                .like(true, Student::getName, "B")
//                .likeValue(true, Student::getName, "B%")
//                .orderBy(true, Student::getName)
//                .with();
//    }
//}
