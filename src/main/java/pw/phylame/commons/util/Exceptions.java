/*
 * Copyright 2017 Peng Wan <phylame@163.com>
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

package pw.phylame.commons.util;

import lombok.NonNull;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static java.lang.String.format;

/**
 * Utilities for creating exception.
 */
public class Exceptions {
    public static IllegalArgumentException forIllegalArgument(String format, Object... args) {
        return new IllegalArgumentException(format(format, args));
    }

    public static IllegalStateException forIllegalState(String format, Object... args) {
        return new IllegalStateException(format(format, args));
    }

    public static RuntimeException forRuntime(String format, Object... args) {
        return new RuntimeException(format(format, args));
    }

    public static IOException forIO(String format, Object... args) {
        return new IOException(format(format, args));
    }

    public static FileNotFoundException forFileNotFound(String format, Object... args) {
        return new FileNotFoundException(format(format, args));
    }

    public static UnsupportedOperationException forUnsupportedOperation(String format, Object... args) {
        return new UnsupportedOperationException(format(format, args));
    }

    public static String dumpToString(@NonNull Throwable t) {
        val out = new StringWriter();
        t.printStackTrace(new PrintWriter(out));
        return out.toString();
    }
}
