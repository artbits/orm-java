package api;

import com.github.artbits.orm.Column;
import com.github.artbits.orm.Config;
import com.github.artbits.orm.DB;
import com.github.artbits.orm.Options;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class APITest {


    public static class Person {
        public Long id;
        @Column(index = true)
        public Long uid;
        public String name;
        public Integer age;
        public Boolean vip;

        public Person(Consumer<Person> consumer) {
            consumer.accept(this);
        }
    }


    public static class Book {
        public Long id;
        public String name;
        public String author;
        public Double price;

        public Book(Consumer<Book> consumer) {
            consumer.accept(this);
        }
    }


    DB connect() {
        Config config = Config.of(c -> c.url = "jdbc:sqlite:example.db");

        DB db = DB.connect(config);
        db.tables(Person.class, Book.class);
        db.deleteAll(Person.class);
        return db;
    }


    @Test
    void create() {
        Config config = Config.of(c -> c.url = "jdbc:sqlite:example.db");

        DB db = DB.connect(config);
        db.tables(Person.class, Book.class);
    }



    @Test
    void insert() {
        List<Person> persons = Arrays.asList(new Person(p -> {
            p.name = "user1";
            p.uid = 1L;
            p.age = 18;
            p.vip = false;
        }), new Person(p -> {
            p.name = "user2";
            p.uid = 2L;
            p.age = 20;
            p.vip = true;
        }), new Person(p -> {
            p.name = "user3";
            p.uid = 3L;
            p.age = 22;
            p.vip = false;
        }), new Person(p -> {
            p.name = "user4";
            p.uid = 4L;
            p.age = 24;
            p.vip = true;
        }), new Person(p -> {
            p.name = "user5";
            p.uid = 5L;
            p.age = 26;
            p.vip = true;
        }));

        DB db = connect();
        persons.forEach(db::insert);
    }



    @Test
    void updateById() {
        DB db = connect();
        insert();
        Person person = db.first(Person.class);
        DB.print(person);

        person.age = 60;
        db.update(person, person.id);
        person = db.first(Person.class);
        DB.print(person);
    }



    @Test
    void updateByCondition() {
        DB db = connect();
        insert();
        db.update(new Person(p -> p.age = 70), "name = ?", "user4");
    }



    @Test
    void findOneById() {
        DB db = connect();
        insert();
        Person person = db.findOne(Person.class, 2L);
        DB.print(person);
    }


    @Test
    void findOneByCondition() {
        DB db = connect();
        insert();
        Person person = db.findOne(Person.class, "name = ?", "user3");
        DB.print(person);
    }


    @Test
    void findAll() {
        DB db = connect();
        insert();
        List<Person> persons = db.findAll(Person.class);
        persons.forEach(DB::print);
    }


    @Test
    void findByIds() {
        DB db = connect();
        insert();
        List<Person> persons = db.find(Person.class, 2L, 1L);
        persons.forEach(DB::print);
    }


    @Test
    void findByIdList() {
        DB db = connect();
        insert();
        List<Person> persons = db.find(Person.class, Arrays.asList(1L, 2L));
        persons.forEach(DB::print);
    }


    @Test
    void find() {
        DB db = connect();
        insert();
        List<Person> persons = db.find(Person.class, options -> options
                .select("name", "age")
                .where("age <= ? && vip = ?", 50, true)
                .order("age", Options.DESC)
                .limit(5)
                .offset(0));
        persons.forEach(DB::print);
    }


    @Test
    void deleteAll() {
        DB db = connect();
        insert();
        db.deleteAll(Person.class);
    }


    @Test
    void deleteByIdList() {
        DB db = connect();
        insert();
        db.delete(Person.class, Arrays.asList(1L, 4L));
    }


    @Test
    void deleteByCondition() {
        DB db = connect();
        insert();
        db.delete(Person.class, "name = ?", "user3");
    }


    @Test
    void deleteByIds() {
        DB db = connect();
        insert();
        db.delete(Person.class, 4L);
    }


    @Test
    void first() {
        DB db = connect();
        insert();

        Person person1 = db.first(Person.class);
        DB.print(person1);

        Person person2 = db.first(Person.class, "vip = ?", true);
        DB.print(person2);
    }


    @Test
    void last() {
        DB db = connect();
        insert();

        Person person1 = db.last(Person.class);
        DB.print(person1);

        Person person2 = db.last(Person.class, "vip = ?", false);
        DB.print(person2);
    }


    @Test
    void count() {
        DB db = connect();
        insert();

        long count1 = db.count(Person.class);
        System.out.println(count1);

        long count2 = db.count(Person.class, "vip = ?", true);
        System.out.println(count2);
    }


    @Test
    void average() {
        DB db = connect();
        insert();

        double d1 = db.average(Person.class, "age");
        System.out.println(d1);

        double d2 = db.average(Person.class, "age", "vip = ?", false);
        System.out.println(d2);
    }


    @Test
    void sum() {
        DB db = connect();
        insert();

        int i1 = db.sum(Person.class, "age").intValue();
        System.out.println(i1);

        int i2 = db.sum(Person.class, "age", "vip = ?", false).intValue();
        System.out.println(i2);
    }


    @Test
    void max() {
        DB db = connect();
        insert();

        int age1 = db.max(Person.class, "age").intValue();
        System.out.println(age1);

        int age2 = db.max(Person.class, "age", "vip = ?", false).intValue();
        System.out.println(age2);
    }


    @Test
    void min() {
        DB db = connect();
        insert();

        int age1 = db.min(Person.class, "age").intValue();
        System.out.println(age1);

        int age2 = db.min(Person.class, "age", "vip = ?", true).intValue();
        System.out.println(age2);
    }


    @Test
    void drop() {
        DB db = connect();
        db.drop(Person.class);
    }

}