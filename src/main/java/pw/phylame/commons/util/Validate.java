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

/**
 * Utilities for validation.
 */
public final class Validate {
    private Validate() {
    }

    public static void require(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void require(boolean condition, String format, Object... args) {
        if (!condition) {
            throw Exceptions.forIllegalArgument(format, args);
        }
    }

    public static void requireNotNull(Object obj) {
        requireNotNull(obj, "object cannot be null");
    }

    public static void requireNotNull(Object obj, String msg) {
        require(obj != null, msg);
    }

    public static void requireNotNull(Object obj, String msg, Object... args) {
        require(obj != null, msg, args);
    }

    public static void requireNotEmpty(CharSequence cs) {
        requireNotEmpty(cs, "object cannot be null");
    }

    public static void requireNotEmpty(CharSequence cs, String msg) {
        require(StringUtils.isNotEmpty(cs), msg);
    }

    public static void requireNotEmpty(CharSequence cs, String msg, Object... args) {
        require(StringUtils.isNotEmpty(cs), msg, args);
    }

    public static void check(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalStateException(msg);
        }
    }

    public static void check(boolean condition, String format, Object... args) {
        if (!condition) {
            throw Exceptions.forIllegalState(format, args);
        }
    }

    public static void checkNotNull(Object o) {
        checkNotNull(o, "object cannot be null");
    }

    public static void checkNotNull(Object o, String msg) {
        check(o != null, msg);
    }

    public static void checkNotNull(Object o, String msg, Object... args) {
        check(o != null, msg, args);
    }
}
