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

import java.util.function.Consumer;

public final class Config {
    public String url;
    public String driver;
    public String username;
    public String password;
    public int initSize = 30;
    public int maxSize = 200;
    public int minIdle = 10;
    public int maxIdle = 20;


    public interface Driver {
        String SQLITE = "org.sqlite.JDBC";
        String MYSQL = "com.mysql.cj.jdbc.Driver";
//        String POSTGRESQL = "org.postgresql.Driver";
//        String SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        String ORACLE = "oracle.jdbc.driver.OracleDriver";
//        String H2 = "org.h2.Driver";
    }


    private Config() { }


    public static Config of(Consumer<Config> consumer) {
        Config config = new Config();
        consumer.accept(config);
        return config;
    }
}
