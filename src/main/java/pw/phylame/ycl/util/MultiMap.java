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
import lombok.val;

import java.util.*;

public class MultiMap<K, V> implements Map<K, Collection<V>> {
    /**
     * Underlying map.
     */
    @NonNull
    private final Map<K, Collection<V>> map;

    /**
     * Type of the collection.
     */
    @NonNull
    private final Class<?> type;

    public MultiMap() {
        map = new HashMap<>();
        type = ArrayList.class;
    }

    public MultiMap(Map<K, Collection<V>> m) {
        this.map = m;
        type = ArrayList.class;
    }

    public MultiMap(Map<K, Collection<V>> m, Class<?> type) {
        this.map = m;
        this.type = type;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Collection) {
            return map.containsValue(value);
        } else {
            for (Collection<?> c : map.values()) {
                if (c != null && c.contains(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Collection<V> get(Object key) {
        return map.get(key);
    }

    public V getOne(K key) {
        val c = map.get(key);
        return CollectUtils.isNotEmpty(c) ? CollectUtils.firstOf(c) : null;
    }

    @Override
    public Collection<V> put(K key, Collection<V> value) {
        return map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows({InstantiationException.class, IllegalAccessException.class})
    public Collection<V> putOne(K key, V value) {
        final Collection<V> prev = map.get(key), c = (Collection<V>) type.newInstance();
        c.add(value);
        map.put(key, c);
        return prev;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows({InstantiationException.class, IllegalAccessException.class})
    public void addOne(K key, V value) {
        Collection<V> c = map.get(key);
        if (c == null) {
            map.put(key, c = (Collection<V>) type.newInstance());
        }
        c.add(value);
    }

    @Override
    public Collection<V> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Collection<V>> m) {
        this.map.putAll(m);
    }

    public void update(Map<? extends K, ? extends V> m) {
        for (val e : m.entrySet()) {
            addOne(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Collection<V>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiMap<?, ?> multiMap = (MultiMap<?, ?>) o;

        return map.equals(multiMap.map) && type.equals(multiMap.type);
    }

    @Override
    public int hashCode() {
        int result = map.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
