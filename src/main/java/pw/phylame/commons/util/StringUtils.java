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

package pw.phylame.commons.util;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.format.Render;
import pw.phylame.commons.value.Pair;

import java.util.*;

public final class StringUtils {
    private StringUtils() {
    }

    public static final String EMPTY_TEXT = "";

    /**
     * Chinese paragraph space character.
     */
    public static final char CHINESE_SPACE = '\u3000';

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        if (isEmpty(cs)) {
            return true;
        }
        char ch;
        for (int i = 0, end = cs.length(); i != end; ++i) {
            ch = cs.charAt(i);
            if (ch != CHINESE_SPACE && !Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String toString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    public static String toString(Object obj, @NonNull Object fallback) {
        return obj != null ? obj.toString() : fallback.toString();
    }

    public static String notEmptyOr(CharSequence cs, @NonNull Object fallback) {
        return isNotEmpty(cs) ? cs.toString() : fallback.toString();
    }

    public static String notEmptyOr(CharSequence cs, @NonNull CharSequence format, Object... args) {
        return isNotEmpty(cs) ? cs.toString() : String.format(format.toString(), args);
    }

    /**
     * Returns a copy of {@code cs} that first letter was converted to upper case.
     *
     * @param cs the string
     * @return string which first character is upper
     */
    public static String capitalized(CharSequence cs) {
        return isEmpty(cs)
                ? toString(cs)
                : String.valueOf(Character.toTitleCase(cs.charAt(0))) + cs.subSequence(1, cs.length());
    }

    public static String uncapitalized(CharSequence cs) {
        return isEmpty(cs)
                ? toString(cs)
                : String.valueOf(Character.toLowerCase(cs.charAt(0))) + cs.subSequence(1, cs.length());
    }

    public static String camelized(CharSequence cs) {
        return uncapitalized(cs);
    }

    /**
     * Returns a copy of {@code cs} that each word was converted to capital.
     *
     * @param cs the string
     * @return string which each word is capital
     */
    public static String titled(CharSequence cs) {
        if (isEmpty(cs)) {
            return toString(cs);
        }
        val b = new StringBuilder(cs.length());
        boolean isFirst = true;
        for (int i = 0, end = cs.length(); i != end; ++i) {
            char ch = cs.charAt(i);
            if (!Character.isLetter(ch)) {
                isFirst = true;
            } else if (isFirst) {
                ch = Character.toTitleCase(ch);
                isFirst = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            b.append(ch);
        }
        return b.toString();
    }

    /**
     * Like {@link String#trim()} but removes Chinese paragraph prefix (u3000).
     *
     * @param cs the input string
     * @return the string removed space
     */
    public static String trimmed(CharSequence cs) {
        if (isEmpty(cs)) {
            return toString(cs);
        }
        int len = cs.length();
        int st = 0;

        char ch;
        while ((st < len) && (((ch = cs.charAt(st)) <= ' ') || (ch == CHINESE_SPACE))) {
            st++;
        }
        while ((st < len) && (((ch = cs.charAt(len - 1)) <= ' ') || (ch == CHINESE_SPACE))) {
            len--;
        }
        return ((st > 0) || (len < cs.length())) ? cs.subSequence(st, len).toString() : toString(cs);
    }

    /**
     * Tests if all characters of specified string are upper case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are upper case or <tt>false</tt> if contains lower case character(s)
     */
    public static boolean isLowerCase(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        for (int i = 0, end = cs.length(); i != end; ++i) {
            if (Character.isUpperCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if all characters of specified string are lower case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are lower case or <tt>false</tt> if contains upper case character(s)
     */
    public static boolean isUpperCase(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        for (int i = 0, end = cs.length(); i != end; ++i) {
            if (Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> String join(CharSequence separator, T... items) {
        return join(separator, items, null);
    }

    public static <T> String join(CharSequence separator, T[] items, Render<? super T> transform) {
        if (items == null || items.length == 0) {
            return EMPTY_TEXT;
        }
        return StringJoiner.<T>builder()
                .iterator(Arrays.asList(items).iterator())
                .separator(separator)
                .transform(transform)
                .build()
                .join();
    }

    public static <T> String join(CharSequence separator, Iterable<T> i) {
        if (i == null) {
            return EMPTY_TEXT;
        }
        return StringJoiner.<T>builder()
                .iterator(i.iterator())
                .separator(separator)
                .build()
                .join();
    }

    public static <T> String join(CharSequence separator, Iterable<T> i, Render<? super T> transform) {
        if (i == null) {
            return EMPTY_TEXT;
        }
        return StringJoiner.<T>builder()
                .iterator(i.iterator())
                .separator(separator)
                .transform(transform)
                .build()
                .join();
    }

    public static <T> String join(CharSequence separator, Iterator<T> i) {
        if (i == null) {
            return EMPTY_TEXT;
        }
        return StringJoiner.<T>builder()
                .iterator(i)
                .separator(separator)
                .build()
                .join();
    }

    public static <T> String join(CharSequence separator, Iterator<T> i, Render<? super T> transform) {
        if (i == null) {
            return EMPTY_TEXT;
        }
        return StringJoiner.<T>builder()
                .iterator(i)
                .separator(separator)
                .transform(transform)
                .build()
                .join();
    }

    public static String multiplyOf(CharSequence cs, int count) {
        if (count <= 0) {
            return EMPTY_TEXT;
        } else if (count == 1) {
            return cs.toString();
        }
        val str = String.valueOf(cs);
        val b = new StringBuilder();
        for (int i = 0; i != count; i++) {
            b.append(str);
        }
        return b.toString();
    }

    /**
     * Returns list of lines split from text content in this object.
     *
     * @param cs        the input string
     * @param skipEmpty {@literal true} to skip empty line
     * @return list of lines, never {@code null}
     * @throws NullPointerException if the {@code cs} is {@code null}
     */
    public static List<String> splitLines(@NonNull CharSequence cs, boolean skipEmpty) {
        val lines = new LinkedList<String>();
        splitLines(cs, lines, skipEmpty);
        return lines;
    }

    public static void splitLines(@NonNull CharSequence cs, List<String> lines, boolean skipEmpty) {
        int i, begin = 0;
        val end = cs.length();
        CharSequence sub;
        for (i = 0; i < end; ) {
            val ch = cs.charAt(i);
            if ('\n' == ch) { // \n
                sub = cs.subSequence(begin, i);
                if (sub.length() > 0 || !skipEmpty) {
                    lines.add(sub.toString());
                }
                begin = ++i;
            } else if ('\r' == ch) {
                sub = cs.subSequence(begin, i);
                if (sub.length() > 0 || !skipEmpty) {
                    lines.add(sub.toString());
                }
                if (i + 1 < end && '\n' == cs.charAt(i + 1)) { // \r\n
                    begin = i += 2;
                } else { // \r
                    begin = ++i;
                }
            } else {
                ++i;
            }
        }
        if (i >= begin) {
            sub = cs.subSequence(begin, cs.length());
            if (sub.length() > 0 || !skipEmpty) {
                lines.add(sub.toString());
            }
        }
    }

    private static Pair<String, String> pairOf(String first, String second) {
        return new Pair<>(first, second);
    }

    public static Pair<String, String> partition(@NonNull String str, @NonNull String separator) {
        val index = str.indexOf(separator);
        return index < 0
                ? pairOf(str, EMPTY_TEXT)
                : pairOf(str.substring(0, index), str.substring(index + separator.length()));
    }

    public static String firstPartOf(String str, String separator) {
        return partition(str, separator).getFirst();
    }

    public static String secondPartOf(String str, String separator) {
        return partition(str, separator).getSecond();
    }

    public static List<Pair<String, String>> getNamedPairs(String str, String partSeparator) {
        return getNamedPairs(str, partSeparator, "=");
    }

    public static List<Pair<String, String>> getNamedPairs(@NonNull String str,
                                                           @NonNull String partSeparator,
                                                           @NonNull String valueSeparator) {
        val pairs = new ArrayList<Pair<String, String>>();
        int index;
        for (val part : str.split(partSeparator)) {
            index = part.indexOf(valueSeparator);
            if (index != -1) {
                pairs.add(pairOf(part.substring(0, index), part.substring(index + valueSeparator.length())));
            } else {
                pairs.add(pairOf(part, EMPTY_TEXT));
            }
        }
        return pairs;
    }

    public static String valueOfName(String str, String name, String partSeparator) {
        return valueOfName(str, name, partSeparator, "=", true);
    }

    public static String valueOfName(String str, String name, String partSeparator, boolean ignoreCase) {
        return valueOfName(str, name, partSeparator, "=", ignoreCase);
    }

    public static String valueOfName(@NonNull String str,
                                     @NonNull String name,
                                     @NonNull String partSeparator,
                                     @NonNull String valueSeparator,
                                     boolean ignoreCase) {
        for (val part : str.split(partSeparator)) {
            val index = part.trim().indexOf(valueSeparator);
            if (index != -1) {
                val tag = part.substring(0, index);
                if (ignoreCase && tag.equalsIgnoreCase(name) || tag.equals(name)) {
                    return part.substring(index + 1);
                }
            }
        }
        return null;
    }

    public static String[] valuesOfName(String str, String name, String partSeparator) {
        return valuesOfName(str, name, partSeparator, "=", true);
    }

    public static String[] valuesOfName(String str, String name, String partSeparator, boolean ignoreCase) {
        return valuesOfName(str, name, partSeparator, "=", ignoreCase);
    }

    public static String[] valuesOfName(@NonNull String str,
                                        @NonNull String name,
                                        @NonNull String partSeparator,
                                        @NonNull String valueSeparator,
                                        boolean ignoreCase) {
        val result = new ArrayList<String>();
        for (val part : str.split(partSeparator)) {
            val index = part.trim().indexOf(valueSeparator);
            if (index != -1) {
                val tag = part.substring(0, index);
                if (ignoreCase && tag.equalsIgnoreCase(name) || tag.equals(name)) {
                    result.add(part.substring(index + 1));
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Builder
    public static class StringJoiner<T> {
        @NonNull
        private Iterator<T> iterator;

        @NonNull
        private CharSequence separator;

        private Render<? super T> transform;

        private CharSequence prefix;
        private CharSequence suffix;

        public String join() {
            val b = new StringBuilder();
            if (isNotEmpty(prefix)) {
                b.append(prefix);
            }
            while (iterator.hasNext()) {
                b.append(transform != null ? transform.render(iterator.next()) : StringUtils.toString(iterator.next()));
                if (iterator.hasNext()) {
                    b.append(separator);
                }
            }
            if (isNotEmpty(suffix)) {
                b.append(suffix);
            }
            return b.toString();
        }
    }
}
