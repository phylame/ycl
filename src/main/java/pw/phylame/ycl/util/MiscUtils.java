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

import java.util.*;

public final class MiscUtils {
    private MiscUtils() {
    }

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> m) {
        return !isEmpty(m);
    }

    public static <E> E firstOf(@NonNull Iterable<E> i) {
        return firstOf(i.iterator());
    }

    public static <E> E firstOf(@NonNull Iterator<E> i) {
        return i.hasNext() ? i.next() : null;
    }

    public static <E> List<E> listOf(E... elements) {
        return Arrays.asList(elements);
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... elements) {
        val set = new HashSet<E>();
        Collections.addAll(set, elements);
        return Collections.unmodifiableSet(set);
    }

    public static <K, V> Map<K, V> mapOf(Object... objects) {
        val m = new HashMap<K, V>();
        fillMap(m, objects);
        return Collections.unmodifiableMap(m);
    }

    public static <V> Map<Integer, V> mapOf(@NonNull Collection<V> c, int from) {
        val m = new HashMap<Integer, V>();
        for (V v : c) {
            m.put(from++, v);
        }
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void fillMap(Map<K, V> m, Object... objects) {
        if (objects == null || objects.length == 0) {
            return;
        }
        if (objects.length % 2 != 0) {
            throw Exceptions.forIllegalArgument("length(%d) of objects must % 2 = 0", objects.length);
        }
        for (int i = 0; i < objects.length; i += 2) {
            m.put((K) objects[i], (V) objects[i + 1]);
        }
    }
}
