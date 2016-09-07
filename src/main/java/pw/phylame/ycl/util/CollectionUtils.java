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
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import pw.phylame.ycl.io.IOUtils;

import java.io.IOException;
import java.util.*;

public final class CollectionUtils {
    private CollectionUtils() {
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

    public static <E> E firstOf(Iterable<E> i) {
        return i != null ? firstOf(i.iterator()) : null;
    }

    public static <E> E firstOf(Iterator<E> i) {
        return i == null ? null : (i.hasNext() ? i.next() : null);
    }

    public static <E> Iterable<E> iterable(@NonNull Enumeration<?> e, final Function<Object, E> transformer) {
        return new SimpleIterable<>(new EnumerationIterator<>(e, transformer));
    }

    @SafeVarargs
    public static <E> List<E> listOf(E... objects) {
        return Arrays.asList(objects);
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... objects) {
        val set = new HashSet<E>();
        Collections.addAll(set, objects);
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
    public static <K, V> void fillMap(@NonNull Map<K, V> m, Object... objects) {
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

    public static Properties propertiesFor(@NonNull String path, ClassLoader loader) throws IOException {
        val in = IOUtils.openResource(path, loader);
        if (in != null) {
            val prop = new Properties();
            prop.load(in);
            in.close();
            return prop;
        }
        return null;
    }

    @SneakyThrows(IOException.class)
    public static void updateByProperties(@NonNull Map<String, ? super String> m, @NonNull String path, ClassLoader loader) {
        val prop = propertiesFor(path, loader);
        if (prop != null) {
            for (val e : prop.entrySet()) {
                m.put(e.getKey().toString(), e.getValue().toString());
            }
        }
    }

    @Value
    private static class SimpleIterable<E> implements Iterable<E> {
        private final Iterator<E> i;

        @Override
        public Iterator<E> iterator() {
            return i;
        }
    }

    @Value
    private static class EnumerationIterator<E> implements Iterator<E> {
        private final Enumeration<?> e;
        private final Function<Object, E> transformer;

        @Override
        public boolean hasNext() {
            return e.hasMoreElements();
        }

        @Override
        public E next() {
            val item = e.nextElement();
            return transformer != null ? transformer.apply(item) : (E) item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}