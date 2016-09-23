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

public class MultiValueMap<K, V> implements Map<K, Collection<V>> {
    @NonNull
    private final Map<K, Collection<V>> m;

    public MultiValueMap() {
        m = new HashMap<>();
    }

    public MultiValueMap(Map<K, Collection<V>> m) {
        this.m = m;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value instanceof Collection) {
            return m.containsValue(value);
        } else {
            for (Collection<?> c : m.values()) {
                if (c != null && c.contains(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Collection<V> get(Object key) {
        return m.get(key);
    }

    public V getOne(K key) {
        val c = m.get(key);
        return CollectionUtils.isNotEmpty(c) ? CollectionUtils.firstOf(c) : null;
    }

    @Override
    public Collection<V> put(K key, Collection<V> value) {
        return m.put(key, value);
    }

    public Collection<V> putOne(K key, V value) {
        final Collection<V> prev = m.get(key), list = new LinkedList<V>();
        list.add(value);
        m.put(key, list);
        return prev;
    }

    public void addOne(K key, V value) {
        Collection<V> c = m.get(key);
        if (c == null) {
            m.put(key, c = new LinkedList<>());
        }
        c.add(value);
    }

    @Override
    public Collection<V> remove(Object key) {
        return m.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Collection<V>> m) {
        this.m.putAll(m);
    }

    public void update(Map<? extends K, ? extends V> m) {
        for (val e : m.entrySet()) {
            addOne(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public Set<K> keySet() {
        return m.keySet();
    }

    @Override
    public Collection<Collection<V>> values() {
        return m.values();
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet() {
        return m.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return m.equals(o);
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    @Override
    public String toString() {
        return m.toString();
    }
}
