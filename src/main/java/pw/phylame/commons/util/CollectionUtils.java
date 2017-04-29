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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import pw.phylame.commons.function.Function;
import pw.phylame.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static boolean isEmpty(Iterator<?> i) {
        return i == null || !i.hasNext();
    }

    public static boolean isNotEmpty(Iterator<?> i) {
        return !isEmpty(i);
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
        if (i == null) {
            return null;
        } else if (i instanceof RandomAccess) {
            val list = (List<E>) i;
            return list.isEmpty() ? null : list.get(0);
        }
        return firstOf(i.iterator());
    }

    public static <E> E firstOf(Iterator<E> i) {
        return i == null ? null : (i.hasNext() ? i.next() : null);
    }

    public static <E> E lastOf(Iterable<E> i) {
        if (i == null) {
            return null;
        } else if (i instanceof RandomAccess) {
            val list = (List<E>) i;
            return list.isEmpty() ? null : list.get(list.size() - 1);
        } else {
            return lastOf(i.iterator());
        }
    }

    public static <E> E lastOf(Iterator<E> i) {
        if (i == null) {
            return null;
        }
        E obj = null;
        while (i.hasNext()) {
            obj = i.next();
        }
        return obj;
    }

    public static <E> Iterator<E> iterator(@NonNull Enumeration<E> e) {
        return new EnumerationIterator<>(e);
    }

    public static <E> Iterable<E> iterable(@NonNull Enumeration<E> e) {
        return iterable(new EnumerationIterator<>(e));
    }

    public static <E> Iterable<E> iterable(@NonNull final Iterator<E> i) {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return i;
            }
        };
    }

    public static <E, T> Iterator<T> map(@NonNull Iterator<? extends E> i, @NonNull Function<? super E, ? extends T> transform) {
        val results = new LinkedList<T>();
        while (i.hasNext()) {
            results.add(transform.apply(i.next()));
        }
        return results.iterator();
    }

    public static <K, V> V getOrElse(Map<K, V> m, K key, Function<K, ? extends V> supplier) {
        return getOrElse(m, key, false, supplier);
    }

    public static <K, V> V getOrElse(@NonNull Map<K, V> m, K key, boolean nullabe, Function<K, ? extends V> supplier) {
        val value = m.get(key);
        if (value != null || (nullabe && m.containsKey(key))) {
            return value;
        }
        return supplier.apply(key);
    }

    public static <K, V> V getOrPut(Map<K, V> m, K key, Function<K, ? extends V> supplier) {
        return getOrPut(m, key, false, supplier);
    }

    public static <K, V> V getOrPut(@NonNull Map<K, V> m, K key, boolean nullabe, Function<K, ? extends V> supplier) {
        V value = m.get(key);
        if (value != null || (nullabe && m.containsKey(key))) {
            return value;
        }
        value = supplier.apply(key);
        if (value != null || nullabe) {
            m.put(key, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <E> void extend(@NonNull Collection<E> c, Iterable<? extends E> i) {
        if (i == null) {
            return;
        }
        if (i instanceof RandomAccess) {
            val list = (List<? extends E>) i;
            for (int j = 0, end = list.size(); j < end; ++j) {
                c.add(list.get(j));
            }
        } else {
            extend(c, i.iterator());
        }
    }

    public static <E> void extend(@NonNull Collection<E> c, Iterator<? extends E> i) {
        if (i == null) {
            return;
        }
        while (i.hasNext()) {
            c.add(i.next());
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void update(@NonNull Map<K, V> m, Iterable<? extends Map.Entry<? extends K, ? extends V>> i) {
        if (i == null) {
            return;
        }
        if (i instanceof RandomAccess) {
            val list = (List<? extends Map.Entry<? extends K, ? extends V>>) i;
            for (int j = 0, end = list.size(); j < end; ++j) {
                val e = list.get(j);
                m.put(e.getKey(), e.getValue());
            }
        } else {
            update(m, i.iterator());
        }
    }

    public static <K, V> void update(@NonNull Map<K, V> m, Iterator<? extends Map.Entry<? extends K, ? extends V>> i) {
        if (i == null) {
            return;
        }
        while (i.hasNext()) {
            val e = i.next();
            m.put(e.getKey(), e.getValue());
        }
    }

    public static void update(@NonNull Map<String, ? super String> m, Properties p) {
        if (isEmpty(p)) {
            return;
        }
        for (val e : p.entrySet()) {
            m.put(e.getKey().toString(), StringUtils.toString(e.getValue()));
        }

    }

    @SafeVarargs
    public static <E> List<E> listOf(E... items) {
        return Arrays.asList(items);
    }

    public static <E> List<E> listOf(Iterable<? extends E> i) {
        if (i == null) {
            return Collections.emptyList();
        } else {
            return listOf(i.iterator());
        }
    }

    public static <E> List<E> listOf(Iterator<? extends E> i) {
        if (i == null) {
            return Collections.emptyList();
        } else {
            val list = new ArrayList<E>();
            extend(list, i);
            return Collections.unmodifiableList(list);
        }
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... items) {
        val set = new HashSet<E>();
        Collections.addAll(set, items);
        return Collections.unmodifiableSet(set);
    }

    public static <E> Set<E> setOf(Iterable<? extends E> i) {
        if (i == null) {
            return Collections.emptySet();
        } else {
            return setOf(i.iterator());
        }
    }

    public static <E> Set<E> setOf(Iterator<? extends E> i) {
        if (i == null) {
            return Collections.emptySet();
        } else {
            val set = new HashSet<E>();
            extend(set, i);
            return Collections.unmodifiableSet(set);
        }
    }

    public static <K, V> Map<K, V> mapOf(Object... items) {
        val m = new HashMap<K, V>();
        fillMap(m, items);
        return Collections.unmodifiableMap(m);
    }

    public static <K, V> Map<K, V> mapOf(@NonNull Collection<V> c) {
        val m = new HashMap<K, V>();
        fillMap(m, c);
        return Collections.unmodifiableMap(m);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void fillMap(@NonNull Map<K, V> m, Collection<?> c) {
        if (isEmpty(c)) {
            return;
        }
        val size = c.size();
        if (size % 2 != 0) {
            throw Exceptions.forIllegalArgument("length(%d) of objects must % 2 = 0", size);
        }
        for (Iterator<?> i = c.iterator(); i.hasNext(); ) {
            m.put((K) i.next(), (V) i.next());
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> void fillMap(@NonNull Map<K, V> m, Object... items) {
        if (items == null || items.length == 0) {
            return;
        }
        val size = items.length;
        if (size % 2 != 0) {
            throw Exceptions.forIllegalArgument("length(%d) of objects must % 2 = 0", size);
        }
        for (int i = 0; i < size; i += 2) {
            m.put((K) items[i], (V) items[i + 1]);
        }
    }

    public static Properties propertiesFor(@NonNull String path) throws IOException {
        return propertiesFor(path, null);
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

    @RequiredArgsConstructor
    private static class EnumerationIterator<E> implements Iterator<E> {
        private final Enumeration<E> e;

        @Override
        public boolean hasNext() {
            return e.hasMoreElements();
        }

        @Override
        public E next() {
            return e.nextElement();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
