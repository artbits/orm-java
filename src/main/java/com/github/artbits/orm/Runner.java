/**
 * Copyright 2023 Zhang Guanhu
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.artbits.orm;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class Runner {

    private final BasicDataSource source = new BasicDataSource();


    Runner() {
        try {
            Optional.ofNullable(Core.config.url).ifPresent(source::setUrl);
            Optional.ofNullable(Core.config.username).ifPresent(source::setUsername);
            Optional.ofNullable(Core.config.password).ifPresent(source::setPassword);
            source.setInitialSize(Core.config.initSize);
            source.setMaxTotal(Core.config.maxSize);
            source.setMinIdle(Core.config.minIdle);
            source.setMaxIdle(Core.config.maxIdle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    Connection connection() {
        try {
            return source.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    void close() {
        try {
            source.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    long insert(String sql) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (statement.executeUpdate() == 0) {
                return -1;
            }
            if (Core.config.type() == Config.Type.SQLite) {
                try (PreparedStatement statement1 = connection.prepareStatement("select last_insert_rowid()");
                     ResultSet set = statement1.executeQuery()) {
                    return set.next() ? set.getLong(1) : -1;
                }
            } else {
                try (ResultSet set = statement.getGeneratedKeys()) {
                    return set.next() ? set.getLong(1) : -1;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    <T> List<T> query(String sql, Class<T> tClass) {
        return executeQuery(sql, set -> {
            List<T> list = new ArrayList<>();
            while (set.next()) {
                T t = Reflect.toEntity(tClass, set);
                Optional.ofNullable(t).ifPresent(list::add);
            }
            return list;
        });
    }


    void getMetaData(Consumer<DatabaseMetaData> consumer) {
        try (Connection connection = connection()) {
            consumer.accept(connection.getMetaData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    <Y> Y executeQuery(String sql, Function<ResultSet, Y> function) {
        try (Connection connection = connection(); Statement statement = connection.createStatement(); ResultSet set = statement.executeQuery(sql)) {
            return function.on(set);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    void executeUpdate(String sql) {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    void executeUpdate(String sql, Function<Void, Void> function) {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    interface Function<X,Y> {
        Y on(X x) throws Exception;
    }


    interface Consumer<X> {
        void accept(X x) throws Exception;
    }

}
