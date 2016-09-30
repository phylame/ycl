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

package pw.phylame.ycl.log;

import static pw.phylame.ycl.log.Level.DEBUG;
import static pw.phylame.ycl.log.Level.DEFAULT_LEVEL;
import static pw.phylame.ycl.log.Level.ERROR;
import static pw.phylame.ycl.log.Level.FATAL;
import static pw.phylame.ycl.log.Level.INFO;
import static pw.phylame.ycl.log.Level.TRACE;
import static pw.phylame.ycl.log.Level.WARN;

import java.io.PrintStream;
import java.text.MessageFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pw.phylame.ycl.util.Exceptions;

public final class Log {
    private Log() {
    }

    @Getter
    @Setter
    @NonNull
    private static Level level = DEFAULT_LEVEL;

    @Getter
    @Setter
    @NonNull
    private static PrintStream out = System.out;

    @Getter
    @Setter
    @NonNull
    private static PrintStream err = System.err;

    public static boolean isEnable(Level level) {
        return level.getCode() <= Log.level.getCode();
    }

    public static void t(String tag, String format, Object... args) {
        if (isEnable(TRACE))
            out.println(formatText(tag, "t", format, args));
    }

    public static void t(String tag, Throwable t) {
        if (isEnable(TRACE))
            t(tag, Exceptions.dumpThrowable(t));
    }

    public static void d(String tag, String format, Object... args) {
        if (isEnable(DEBUG))
            out.println(formatText(tag, "d", format, args));
    }

    public static void d(String tag, Throwable t) {
        if (isEnable(DEBUG))
            d(tag, Exceptions.dumpThrowable(t));
    }

    public static void i(String tag, String format, Object... args) {
        if (isEnable(INFO))
            out.println(formatText(tag, "i", format, args));
    }

    public static void i(String tag, Throwable t) {
        if (isEnable(INFO))
            i(tag, Exceptions.dumpThrowable(t));
    }

    public static void w(String tag, String format, Object... args) {
        if (isEnable(WARN))
            out.println(formatText(tag, "w", format, args));
    }

    public static void w(String tag, Throwable t) {
        if (isEnable(WARN))
            w(tag, Exceptions.dumpThrowable(t));
    }

    public static void e(String tag, String format, Object... args) {
        if (isEnable(ERROR))
            err.println(formatText(tag, "e", format, args));
    }

    public static void e(String tag, Throwable t) {
        if (isEnable(ERROR))
            e(tag, Exceptions.dumpThrowable(t));
    }

    public static void f(String tag, String format, Object... args) {
        if (isEnable(FATAL))
            err.println(formatText(tag, "f", format, args));
    }

    public static void f(String tag, Throwable t) {
        if (isEnable(FATAL))
            f(tag, Exceptions.dumpThrowable(t));
    }

    private static String formatText(String tag, String level, String format, Object... args) {
        return String.format("[%s] %s/%s: %s", Thread.currentThread().getName(), level, tag,
                MessageFormat.format(format, args));
    }

}
