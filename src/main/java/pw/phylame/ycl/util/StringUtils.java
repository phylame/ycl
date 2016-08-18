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
import pw.phylame.ycl.value.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Chinese paragraph indent character.
     */
    public static final char CHINESE_INDENT = '\u3000';

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    /**
     * Returns a copy of {@code str} that first letter was converted to upper case.
     *
     * @param str the string
     * @return string which first character is upper
     */
    public static String capitalized(String str) {
        return isEmpty(str) ? str : Character.toUpperCase(str.charAt(0)) + str.substring(1, str.length()).toLowerCase();
    }

    public static String camelized(String str) {
        return isEmpty(str) ? str : Character.toLowerCase(str.charAt(0)) + str.substring(1, str.length());
    }

    /**
     * Returns a copy of {@code str} that each word was converted to capital.
     *
     * @param str the string
     * @return string which each word is capital
     */
    public static String titled(String str) {
        if (isEmpty(str)) {
            return toString(str);
        }
        val b = new StringBuilder(str.length());
        boolean isFirst = true;
        val end = str.length();
        for (int i = 0; i < end; ++i) {
            char ch = str.charAt(i);
            if (!Character.isLetter(ch)) {
                isFirst = true;
            } else if (isFirst) {
                ch = Character.toUpperCase(ch);
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
     * @param str the input string
     * @return the string removed space
     */
    public static String trimmed(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int len = str.length();
        int st = 0;

        char ch;
        while ((st < len) && (((ch = str.charAt(st)) <= ' ') || (ch == CHINESE_INDENT))) {
            st++;
        }
        while ((st < len) && (((ch = str.charAt(len - 1)) <= ' ') || (ch == CHINESE_INDENT))) {
            len--;
        }
        return toString(((st > 0) || (len < str.length())) ? str.subSequence(st, len) : str);
    }

    /**
     * Tests if all characters of specified string are upper case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are upper case or
     * <tt>false</tt> if contains lower case character(s)
     */
    public static boolean isLowerCase(@NonNull CharSequence cs) {
        val end = cs.length();
        for (int i = 0; i < end; ++i) {
            /* found upper case */
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
     * @return <tt>true</tt> if all characters are lower case or
     * <tt>false</tt> if contains upper case character(s)
     */
    public static boolean isUpperCase(@NonNull CharSequence cs) {
        val end = cs.length();
        for (int i = 0; i < end; ++i) {
            /* found lower case */
            if (Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static <T> String join(@NonNull CharSequence separator, @NonNull T[] objects) {
        val b = new StringBuilder(objects.length * 8);
        val end = objects.length - 1;
        for (int i = 0; i < end; ++i) {
            b.append(objects[i].toString()).append(separator);
        }
        return b.append(objects[end].toString()).toString();
    }

    public static <T> String join(@NonNull CharSequence separator, @NonNull Collection<T> elements) {
        val b = new StringBuilder(elements.size() * 8);
        val end = elements.size();
        int i = 1;
        for (T object : elements) {
            b.append(object.toString());
            if (i++ != end) {
                b.append(separator);
            }
        }
        return b.toString();
    }

    /**
     * Returns list of lines split from text content in this object.
     *
     * @param cs        the input string
     * @param skipEmpty <code>true</code> to skip empty line
     * @return list of lines, never <code>null</code>
     * @throws NullPointerException if the <code>cs</code> is <code>null</code>
     */
    public static List<String> splitLines(@NonNull CharSequence cs, boolean skipEmpty) {
        val lines = new LinkedList<String>();
        int i, begin = 0;
        val end = cs.length();
        CharSequence sub;
        for (i = 0; i < end; ) {
            val ch = cs.charAt(i);
            if ('\n' == ch) {   // \n
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
                if (i + 1 < end && '\n' == cs.charAt(i + 1)) {   // \r\n
                    begin = i += 2;
                } else {    // \r
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
        return lines;
    }

    public static List<Pair<String, String>> getNamedPairs(String str, String partSeparator) {
        return getNamedPairs(str, partSeparator, "=");
    }

    public static List<Pair<String, String>> getNamedPairs(@NonNull String str, @NonNull String partSeparator,
                                                           @NonNull String valueSeparator) {
        val pairs = new ArrayList<Pair<String, String>>();
        int index;
        for (val part : str.split(partSeparator)) {
            index = part.indexOf(valueSeparator);
            if (index != -1) {
                pairs.add(new Pair<String, String>(part.substring(0, index), part.substring(index + valueSeparator.length())));
            } else {
                pairs.add(new Pair<String, String>(part, ""));
            }
        }
        return pairs;
    }

    public static String firstPartOf(@NonNull String str, @NonNull String sep) {
        val index = str.indexOf(sep);
        return index < 0 ? str : str.substring(0, index);
    }

    public static String firstPartOf(@NonNull String str, char sep) {
        val index = str.indexOf(sep);
        return index < 0 ? str : str.substring(0, index);
    }

    public static String secondPartOf(@NonNull String str, @NonNull String sep) {
        val index = str.indexOf(sep);
        return index < 0 ? str : str.substring(index + sep.length());
    }

    public static String secondPartOf(@NonNull String str, char sep) {
        val index = str.indexOf(sep);
        return index < 0 ? str : str.substring(index + 1);
    }

    public static String valueOfName(@NonNull String str, @NonNull String name, @NonNull String sep,
                                     boolean ignoreCase, String fallback) {
        for (val part : str.split(sep)) {
            val index = part.trim().indexOf('=');
            if (index != -1) {
                val tag = part.substring(0, index);
                if (ignoreCase && tag.equalsIgnoreCase(name) || tag.equals(name)) {
                    val value = part.substring(index + 1);
                    return isNotEmpty(value) ? value : fallback;
                }
            }
        }
        return null;
    }

    public static String[] valuesOfName(@NonNull String str, @NonNull String name, @NonNull String sep,
                                        boolean ignoreCase) {
        val result = new ArrayList<String>();
        for (val part : str.split(sep)) {
            val index = part.trim().indexOf('=');
            if (index != -1) {
                val tag = part.substring(0, index);
                if (ignoreCase && tag.equalsIgnoreCase(name) || tag.equals(name)) {
                    result.add(part.substring(index + 1));
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
