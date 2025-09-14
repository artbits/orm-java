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

import java.util.*;
import java.util.function.Consumer;

final class Core implements DB {

    private final Runner runner;
    private final Config config;


    Core(Config config) {
        this.config = config;
        runner = new Runner(config);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }


    @Override
    public void close() {
        runner.close();
    }


    @Override
    public void tables(Class<?>... classes) {
        TableManager.init(runner, config, classes);
    }


    @Override
    public void drop(Class<?>... classes) {
        for (Class<?> tClass : classes) {
            runner.executeUpdate(SQLTemplate.drop(tClass));
        }
    }


    @Override
    public <T> long insert(T t) {
        String sql = SQLTemplate.insert(t);
        return runner.insert(sql);
    }


    @Override
    public <T> void update(T t, String predicate, Object... args) {
        String sql = SQLTemplate.update(t, new Options().where(predicate, args));
        runner.executeUpdate(sql, unused -> null);
    }


    @Override
    public <T> void update(T t, long id) {
        update(t, "id = ?", id);
    }


    @Override
    public <T> void delete(Class<T> tClass, String predicate, Object... args) {
        String sql = SQLTemplate.delete(tClass, new Options().where(predicate, args));
        runner.executeUpdate(sql, unused -> null);
    }


    @Override
    public <T> void delete(Class<T> tClass, List<Long> ids) {
        StringBuilder builder = new StringBuilder(String.valueOf(ids));
        builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
        delete(tClass, "id in(?)", builder);
    }


    @Override
    public <T> void delete(Class<T> tClass, Long... ids) {
        delete(tClass, Arrays.asList(ids));
    }


    @Override
    public <T> void deleteAll(Class<T> tClass) {
        delete(tClass, null, (Object) null);
    }


    @Override
    public <T> List<T> find(Class<T> tClass, Consumer<Options> consumer) {
        Options options = (consumer != null) ? new Options() : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        String sql = SQLTemplate.query(tClass, options);
        return runner.query(sql, tClass);
    }


    @Override
    public <T> List<T> find(Class<T> tClass, List<Long> ids) {
        StringBuilder builder = new StringBuilder(String.valueOf(ids));
        builder.deleteCharAt(0).deleteCharAt(builder.length() - 1);
        return find(tClass, options -> options.where("id in(?)", builder));
    }


    @Override
    public <T> List<T> find(Class<T> tClass, Long... ids) {
        return find(tClass, Arrays.asList(ids));
    }


    @Override
    public <T> List<T> findAll(Class<T> tClass) {
        return find(tClass, (Consumer<Options>) null);
    }


    @Override
    public <T> T findOne(Class<T> tClass, String predicate, Object... args) {
        List<T> list = find(tClass, options -> options.where(predicate, args));
        return (!list.isEmpty()) ? list.get(0) : null;
    }


    @Override
    public <T> T findOne(Class<T> tClass, Long id) {
        return findOne(tClass, "id = ?", id);
    }


    @Override
    public <T> T first(Class<T> tClass, String predicate, Object... args) {
        List<T> list = find(tClass, options -> options.where(predicate, args).order("id", Options.ASC));
        return (!list.isEmpty()) ? list.get(0) : null;
    }


    @Override
    public <T> T first(Class<T> tClass) {
        return first(tClass, null, (Object) null);
    }


    @Override
    public <T> T last(Class<T> tClass, String predicate, Object... args) {
        List<T> list = find(tClass, options -> options.where(predicate, args).order("id", Options.DESC));
        return (!list.isEmpty()) ? list.get(0) : null;
    }


    @Override
    public <T> T last(Class<T> tClass) {
        return last(tClass, null, (Object) null);
    }


    @Override
    public <T> long count(Class<T> tClass, String predicate, Object... args) {
        String s = SQLTemplate.query(tClass, new Options().select("count(*)").where(predicate, args));
        return runner.executeQuery(s, set -> set.next() ? set.getLong(1) : 0);
    }


    @Override
    public <T> long count(Class<T> tClass) {
        return count(tClass, null, (Object) null);
    }


    @Override
    public <T> double average(Class<T> tClass, String column, String predicate, Object... args) {
        String s = SQLTemplate.query(tClass, new Options().select(String.format("avg(%s)", column)).where(predicate, args));
        return runner.executeQuery(s, set -> set.next() ? set.getDouble(1) : 0);
    }


    @Override
    public <T> double average(Class<T> tClass, String column) {
        return average(tClass, column, null, (Object) null);
    }


    @Override
    public <T> Number sum(Class<T> tClass, String column, String predicate, Object... args) {
        String s = SQLTemplate.query(tClass, new Options().select(String.format("sum(%s)", column)).where(predicate, args));
        return runner.executeQuery(s, set -> set.next() ? (Number) set.getObject(1) : 0);
    }


    @Override
    public <T> Number sum(Class<T> tClass, String column) {
        return sum(tClass, column, null, (Object) null);
    }


    @Override
    public <T> Number max(Class<T> tClass, String column, String predicate, Object... args) {
        String s = SQLTemplate.query(tClass, new Options().select(String.format("max(%s)", column)).where(predicate, args));
        return runner.executeQuery(s, set -> set.next() ? (Number) set.getObject(1) : 0);
    }


    @Override
    public <T> Number max(Class<T> tClass, String column) {
        return max(tClass, column, null, (Object) null);
    }


    @Override
    public <T> Number min(Class<T> tClass, String column, String predicate, Object... args) {
        String s = SQLTemplate.query(tClass, new Options().select(String.format("min(%s)", column)).where(predicate, args));
        return runner.executeQuery(s, set -> set.next() ? (Number) set.getObject(1) : 0);
    }


    @Override
    public <T> Number min(Class<T> tClass, String column) {
        return min(tClass, column, null, (Object) null);
    }

}
