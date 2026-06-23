package xyz.erupt.test.core;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import xyz.erupt.test.EruptApplicationTests;
import xyz.erupt.test.model.query.Grade;
import xyz.erupt.test.model.query.Student;

import java.util.List;

@Rollback
@Transactional
public class LambdaQueryTest extends EruptApplicationTests {

    private Student newStudent(String name, int age) {
        Student s = new Student();
        s.setName(name);
        s.setAge(age);
        eruptDao.persist(s);
        return s;
    }

    @Test
    void orQueryTest() {
        newStudent("Alice", 18);
        newStudent("Bob", 20);
        newStudent("Carol", 22);
        eruptDao.getEntityManager().flush();

        // OR matches two of the three students
        List<Student> result = eruptDao.lambdaQuery(Student.class)
                .or(q -> q.eq(Student::getName, "Alice").eq(Student::getName, "Bob"))
                .orderBy(Student::getName)
                .list();
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Alice", result.get(0).getName());
        Assertions.assertEquals("Bob", result.get(1).getName());

        // AND narrows the OR result
        List<Student> narrowed = eruptDao.lambdaQuery(Student.class)
                .ge(Student::getAge, 20)
                .or(q -> q.eq(Student::getName, "Bob").eq(Student::getName, "Carol"))
                .list();
        Assertions.assertEquals(2, narrowed.size());

        // OR with no match returns empty
        List<Student> empty = eruptDao.lambdaQuery(Student.class)
                .or(q -> q.eq(Student::getName, "Nobody").eq(Student::getName, "Ghost"))
                .list();
        Assertions.assertTrue(empty.isEmpty());

        // Empty consumer is a no-op (no extra AND clause added)
        List<Student> all = eruptDao.lambdaQuery(Student.class)
                .or(q -> {})
                .list();
        Assertions.assertEquals(3, all.size());
    }

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
