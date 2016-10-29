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

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pw.phylame.ycl.util.Exceptions;

import java.io.IOException;

import static pw.phylame.ycl.log.Level.*;

@Getter
@Setter
public final class Log {
    private Log() {
    }

    @NonNull
    private static Level level = DEFAULT;

    @NonNull
    private static Appendable out = System.out;

    @NonNull
    private static Appendable err = System.err;

    @NonNull
    private static Formatter formatter = new DefaultFormatter();

    public static boolean isEnable(Level level) {
        return level.getCode() <= Log.level.getCode();
    }

    public static void t(String tag, String format, Object... args) {
        if (isEnable(TRACE))
            printToOut(formatter.format(tag, "t", format, args), true);
    }

    public static void t(String tag, Throwable t) {
        if (isEnable(TRACE))
            t(tag, Exceptions.dumpThrowable(t));
    }

    public static void d(String tag, String format, Object... args) {
        if (isEnable(DEBUG))
            printToOut(formatter.format(tag, "d", format, args), true);
    }

    public static void d(String tag, Throwable t) {
        if (isEnable(DEBUG))
            d(tag, Exceptions.dumpThrowable(t));
    }

    public static void i(String tag, String format, Object... args) {
        if (isEnable(INFO))
            printToOut(formatter.format(tag, "i", format, args), true);
    }

    public static void i(String tag, Throwable t) {
        if (isEnable(INFO))
            i(tag, Exceptions.dumpThrowable(t));
    }

    public static void w(String tag, String format, Object... args) {
        if (isEnable(WARN))
            printToOut(formatter.format(tag, "w", format, args), true);
    }

    public static void w(String tag, Throwable t) {
        if (isEnable(WARN))
            w(tag, Exceptions.dumpThrowable(t));
    }

    public static void e(String tag, String format, Object... args) {
        if (isEnable(ERROR))
            printToErr(formatter.format(tag, "e", format, args), true);
    }

    public static void e(String tag, Throwable t) {
        if (isEnable(ERROR))
            e(tag, Exceptions.dumpThrowable(t));
    }

    public static void f(String tag, String format, Object... args) {
        if (isEnable(FATAL))
            printToErr(formatter.format(tag, "f", format, args), true);
    }

    public static void f(String tag, Throwable t) {
        if (isEnable(FATAL))
            f(tag, Exceptions.dumpThrowable(t));
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static void printToOut(String text, boolean withNewLine) {
        print(out, text, withNewLine);
    }

    private static void printToErr(String text, boolean withNewLine) {
        print(err, text, withNewLine);
    }

    private static void print(Appendable out, String text, boolean withNewLine) {
        try {
            out.append(text);
            if (withNewLine) {
                out.append(LINE_SEPARATOR);
            }
        } catch (IOException ignored) {

        }
    }
}
