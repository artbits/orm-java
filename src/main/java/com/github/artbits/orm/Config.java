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
    public String username;
    public String password;
    public int initSize = 30;
    public int maxSize = 200;
    public int minIdle = 10;
    public int maxIdle = 20;


    private Config() { }


    public static Config of(Consumer<Config> consumer) {
        Config config = new Config();
        consumer.accept(config);
        return config;
    }


    int type() {
        if (url.contains("sqlite")) {
            return Type.SQLite;
        } else if (url.contains("mysql")) {
            return Type.MySQL;
        } else if (url.contains("postgresql")) {
            return Type.PostgreSQL;
        } else {
            return 0;
        }
    }


    interface Type {
        int SQLite = 1;
        int MySQL = 2;
        int PostgreSQL = 3;
    }

}
