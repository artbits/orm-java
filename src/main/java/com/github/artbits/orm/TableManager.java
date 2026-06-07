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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

final class TableManager {

    private final static HashMap<String, HashMap<String, String>> tableColumnTypeMap = new HashMap<>();
    private final static HashMap<String, String> indexTypeMap = new HashMap<>();


    static void init(Runner runner, Class<?>... classes) {
        for (Class<?> tClass : classes) {
            createTable(runner, tClass);
            loadMetaData(runner, tClass);
            addColumn(runner, tClass);
            addIndex(runner, tClass);
        }
        dropIndex(runner);
    }


    private static void createTable(Runner runner, Class<?> tClass) {
        runner.executeUpdate(SQLTemplate.create(tClass));
    }


    private static void addColumn(Runner runner, Class<?> tClass) {
        String tableName = tClass.getSimpleName().toLowerCase();
        HashMap<String, String> columnTypeMap = tableColumnTypeMap.getOrDefault(tableName, null);
        Reflect<?> reflect = new Reflect<>(tClass);
        if (columnTypeMap != null) {
            reflect.getDatabaseColumnsWithType((column, type) -> {
                if (columnTypeMap.getOrDefault(column, null) == null) {
                    runner.executeUpdate(SQLTemplate.addColumn(tClass, column, type));
                }
            });
        }
    }


    private static void addIndex(Runner runner, Class<?> tClass) {
        Reflect<?> reflect = new Reflect<>(tClass);
        reflect.getIndexList((index, column) -> {
            if (indexTypeMap.get(index) != null) {
                indexTypeMap.remove(index, column);
            } else {
                runner.executeUpdate(SQLTemplate.createIndex(tClass, column));
            }
        });
    }


    private static void dropIndex(Runner runner) {
        indexTypeMap.forEach((index, column) -> {
            if (Core.config.type() == Config.Type.SQLite) {
                runner.executeUpdate(SQLTemplate.dropIndex(null, index));
            } else if (Core.config.type() == Config.Type.PostgreSQL) {
                if (!index.contains("_pkey")) {
                    runner.executeUpdate(SQLTemplate.dropIndex(null, index));
                }
            } else if (Core.config.type() == Config.Type.MySQL) {
                if (!Objects.equals(index, "PRIMARY")) {
                    String tableName = index.replace("idx_", "").replace(("_" + column), "");
                    runner.executeUpdate(SQLTemplate.dropIndex(tableName, index));
                }
            }
        });
    }


    private static void loadMetaData(Runner runner, Class<?> tClass) {
        String tableName = tClass.getSimpleName().toLowerCase();
        runner.getMetaData(data -> {
            HashMap<String, String> columnTypeMap = new HashMap<>();
            try (ResultSet set = data.getColumns(null, null, tableName, null)) {
                while (set.next()) {
                    String column = set.getString("COLUMN_NAME");
                    String type = set.getString("TYPE_NAME").toLowerCase();
                    columnTypeMap.put(column, type);
                }
            }
            tableColumnTypeMap.put(tableName, columnTypeMap);
            try (ResultSet set = data.getIndexInfo(null, null, tableName, false, false)){
                while (set.next()) {
                    String index = set.getString("INDEX_NAME");
                    String column = set.getString("COLUMN_NAME");
                    Optional.ofNullable(index).ifPresent(i -> indexTypeMap.put(index, column));
                }
            }
        });
    }

}