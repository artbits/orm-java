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

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class Reflect<T> {

    private final Map<String, Field> fieldMap = new LinkedHashMap<>();
    private Class<?> tClass;
    private T t;

    Reflect(Class<?> tClass) {
        this.tClass = tClass;
        newInstance(tClass);
    }


    Reflect(T t) {
        this.t = t;
        newInstance(t.getClass());
    }


    private void newInstance(Class<?> tClass) {
        Class<?> clazz = tClass;
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (!isIgnore(field)) {
                    fieldMap.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }


    void setValue(String fieldName, Object value) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            if (field != null) {
                field.set(t, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    Object getValue(String fieldName) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            return (field != null) ? field.get(t) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    Class<?> getType(String fieldName) {
        Field field = fieldMap.getOrDefault(fieldName, null);
        return field.getType();
    }


    String getDatabaseType(String fieldName) {
        String type = getType(fieldName).getSimpleName().toLowerCase();
        if (Core.config.type() == Config.Type.SQLite) {
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "integer":
                case "long":
                case "boolean": return "integer";
                case "float":
                case "double": return "real";
                case "char":
                case "character":
                case "string": return "text";
                default: throw new NullPointerException();
            }
        } else if (Core.config.type() == Config.Type.MySQL) {
            switch (type) {
                case "byte": return "tinyint";
                case "short": return "smallint";
                case "int":
                case "integer": return "int";
                case "long": return "bigint";
                case "float": return "float";
                case "double": return "double";
                case "char":
                case "character":
                case "string": return "text";
                case "boolean": return "boolean";
                default: throw new NullPointerException();
            }
        } else if (Core.config.type() == Config.Type.PostgreSQL) {
            switch (type) {
                case "byte":
                case "short": return "smallint";
                case "int":
                case "integer": return "int";
                case "long": return "bigint";
                case "float": return "real";
                case "double": return "double precision";
                case "char":
                case "character":
                case "string": return "text";
                case "boolean": return "boolean";
                default: throw new NullPointerException();
            }
        } else {
            throw new RuntimeException("Unsupported database type.");
        }
    }


    Object getDatabaseValue(Field field) {
        try {
            String fieldName = field.getName();
            Field dbField = fieldMap.getOrDefault(fieldName, null);
            Object dbValue = (dbField != null) ? dbField.get(t) : null;
            if (dbField != null && dbValue != null) {
                return getDatabaseType(fieldName).equals("text") ? String.format("'%s'", dbValue) : dbValue;
            }
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    void getDatabaseColumnsWithValue(BiConsumer<String, Object> consumer) {
        for (Field field : fieldMap.values()) {
            consumer.accept(field.getName(), getDatabaseValue(field));
        }
    }


    void getDatabaseColumnsWithType(BiConsumer<String, String> consumer) {
        for (Field field : fieldMap.values()) {
            consumer.accept(field.getName(), getDatabaseType(field.getName()));
        }
    }


    void getIndexList(BiConsumer<String, String> consumer) {
        String table = tClass.getSimpleName().toLowerCase();
        fieldMap.values().forEach(field -> {
            if (isIndex(field)) {
                String column = field.getName();
                String index = String.format("idx_%s_%s", table, column);
                consumer.accept(index, column);
            }
        });
    }


    T get() {
        return t;
    }


    static <T> T toEntity(Class<T> tClass, ResultSet resultSet) {
        try {
            Map<String, Boolean> columnsMap = new HashMap<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                columnsMap.put(metaData.getColumnName(i), true);
            }
            T t = tClass.getConstructor(Consumer.class).newInstance((Consumer<T>) (c -> {}));
            Reflect<T> reflect = new Reflect<>(t);
            for (Field field : reflect.fieldMap.values()) {
                String name = field.getName();
                if (!columnsMap.isEmpty() && !columnsMap.getOrDefault(name, false)) {
                    continue;
                }
                String type = field.getType().getSimpleName().toLowerCase();
                switch (type) {
                    case "int":
                    case "integer":
                        reflect.setValue(name, resultSet.getInt(name));
                        break;
                    case "byte":
                        reflect.setValue(name, resultSet.getByte(name));
                        break;
                    case "short":
                        reflect.setValue(name, resultSet.getShort(name));
                        break;
                    case "long":
                        reflect.setValue(name, resultSet.getLong(name));
                        break;
                    case "float":
                        reflect.setValue(name, resultSet.getFloat(name));
                        break;
                    case "double":
                        reflect.setValue(name, resultSet.getDouble(name));
                        break;
                    case "char":
                    case "character":
                    case "string":
                        reflect.setValue(name, resultSet.getString(name));
                        break;
                    case "boolean" :
                        reflect.setValue(name, resultSet.getBoolean(name));
                        break;
                }
            }
            return reflect.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static boolean isIgnore(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.ignore();
        }
        return false;
    }


    static boolean isIndex(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.index();
        }
        return false;
    }

}