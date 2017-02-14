/*
 * Copyright 2017 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.commons.log;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public enum LogLevel {
    ALL(7, "all"),
    TRACE(6, "trace"),
    DEBUG(5, "debug"),
    INFO(4, "info"),
    WARN(3, "warn"),
    ERROR(2, "error"),
    FATAL(1, "fatal"),
    OFF(0, "off"),
    DEFAULT(INFO.code, "default");

    @Getter
    private final int code;

    @Getter
    private final String name;

    LogLevel(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LogLevel forName(@NonNull String name, LogLevel fallback) {
        for (val level : values()) {
            if (level.getName().equals(name)) {
                return level;
            }
        }
        return fallback;
    }
}
