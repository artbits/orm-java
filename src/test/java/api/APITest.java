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


    public static class User {
        public Long id;
        @Column(index = true)
        public Long uid;
        public String name;
        public Integer age;
        public Boolean vip;

        public User(Consumer<User> consumer) {
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
        Config config = Config.of(c -> {
            c.driver = Config.Driver.SQLITE;
            c.url = "jdbc:sqlite:example.db";
        });

        DB db = DB.connect(config);
        db.tables(User.class, Book.class);
        db.deleteAll(User.class);
        return db;
    }


    @Test
    void create() {
        Config config = Config.of(c -> {
            c.driver = Config.Driver.SQLITE;
            c.url = "jdbc:sqlite:example.db";
        });

        DB db = DB.connect(config);
        db.tables(User.class, Book.class);
    }



    @Test
    void insert() {
        List<User> users = Arrays.asList(new User(u -> {
            u.name = "user1";
            u.age = 18;
            u.vip = false;
        }), new User(u -> {
            u.name = "user2";
            u.age = 20;
            u.vip = true;
        }), new User(u -> {
            u.name = "user3";
            u.age = 22;
            u.vip = false;
        }), new User(u -> {
            u.name = "user4";
            u.age = 24;
            u.vip = true;
        }), new User(u -> {
            u.name = "user5";
            u.age = 26;
            u.vip = true;
        }));

        DB db = connect();
        users.forEach(db::insert);
    }



    @Test
    void updateById() {
        DB db = connect();
        insert();
        User user = db.first(User.class);
        DB.print(user);

        user.age = 60;
        db.update(user, user.id);
        user = db.first(User.class);
        DB.print(user);
    }



    @Test
    void updateByCondition() {
        DB db = connect();
        insert();
        db.update(new User(u -> u.age = 70), "name = ?", "user4");
    }



    @Test
    void findOneById() {
        DB db = connect();
        insert();
        User user = db.findOne(User.class, 2L);
        DB.print(user);
    }


    @Test
    void findOneByCondition() {
        DB db = connect();
        insert();
        User user = db.findOne(User.class, "name = ?", "user3");
        DB.print(user);
    }


    @Test
    void findAll() {
        DB db = connect();
        insert();
        List<User> users = db.findAll(User.class);
        users.forEach(DB::print);
    }


    @Test
    void findByIds() {
        DB db = connect();
        insert();
        List<User> users = db.find(User.class, 2L, 1L);
        users.forEach(DB::print);
    }


    @Test
    void findByIdList() {
        DB db = connect();
        insert();
        List<User> users = db.find(User.class, Arrays.asList(1L, 2L));
        users.forEach(DB::print);
    }


    @Test
    void find() {
        DB db = connect();
        insert();
        List<User> users = db.find(User.class, options -> options
                .select("name", "age")
                .where("age <= ? && vip = ?", 50, true)
                .order("age", Options.DESC)
                .limit(5)
                .offset(0));
        users.forEach(DB::print);
    }


    @Test
    void deleteAll() {
        DB db = connect();
        insert();
        db.deleteAll(User.class);
    }


    @Test
    void deleteByIdList() {
        DB db = connect();
        insert();
        db.delete(User.class, Arrays.asList(1L, 4L));
    }


    @Test
    void deleteByCondition() {
        DB db = connect();
        insert();
        db.delete(User.class, "name = ?", "user3");
    }


    @Test
    void deleteByIds() {
        DB db = connect();
        insert();
        db.delete(User.class, 4L);
    }


    @Test
    void first() {
        DB db = connect();
        insert();

        User user1 = db.first(User.class);
        DB.print(user1);

        User user2 = db.first(User.class, "vip = ?", true);
        DB.print(user2);
    }


    @Test
    void last() {
        DB db = connect();
        insert();

        User user1 = db.last(User.class);
        DB.print(user1);

        User user2 = db.last(User.class, "vip = ?", false);
        DB.print(user2);
    }


    @Test
    void count() {
        DB db = connect();
        insert();

        long count1 = db.count(User.class);
        System.out.println(count1);

        long count2 = db.count(User.class, "vip = ?", true);
        System.out.println(count2);
    }


    @Test
    void average() {
        DB db = connect();
        insert();

        double d1 = db.average(User.class, "age");
        System.out.println(d1);

        double d2 = db.average(User.class, "age", "vip = ?", false);
        System.out.println(d2);
    }


    @Test
    void sum() {
        DB db = connect();
        insert();

        int i1 = db.sum(User.class, "age").intValue();
        System.out.println(i1);

        int i2 = db.sum(User.class, "age", "vip = ?", false).intValue();
        System.out.println(i2);
    }


    @Test
    void max() {
        DB db = connect();
        insert();

        int age1 = db.max(User.class, "age").intValue();
        System.out.println(age1);

        int age2 = db.max(User.class, "age", "vip = ?", false).intValue();
        System.out.println(age2);
    }


    @Test
    void min() {
        DB db = connect();
        insert();

        int age1 = db.min(User.class, "age").intValue();
        System.out.println(age1);

        int age2 = db.min(User.class, "age", "vip = ?", true).intValue();
        System.out.println(age2);
    }


    @Test
    void drop() {
        DB db = connect();
        db.drop(User.class);
    }

}