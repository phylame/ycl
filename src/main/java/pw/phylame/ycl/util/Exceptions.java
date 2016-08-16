/*
 * Copyright 2016 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.ycl.util;

import lombok.NonNull;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities for creating exception.
 */
public final class Exceptions {
    private Exceptions() {
    }

    public static IllegalArgumentException forIllegalArgument(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    public static IllegalStateException forIllegalState(String format, Object... args) {
        return new IllegalStateException(String.format(format, args));
    }

    public static RuntimeException forRuntime(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    public static IOException forIO(String format, Object... args) {
        return new IOException(String.format(format, args));
    }

    public static FileNotFoundException forFileNotFound(String format, Object... args) {
        return new FileNotFoundException(String.format(format, args));
    }

    public static String dumpThrowable(@NonNull Throwable t) {
        val out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }
}