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
import lombok.val;
import pw.phylame.commons.function.BiFunction;
import pw.phylame.commons.io.IOUtils;
import pw.phylame.commons.log.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Implementor<T> {
    private static final String TAG = "IMPs";

    private final Class<T> type;
    private final boolean reusable;
    private final ClassLoader loader;

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ImpHolder<T>> impHolders = new LinkedHashMap<>();

    /**
     * Constructs reusable instance for specified type.
     *
     * @param type class of the type
     */
    public Implementor(@NonNull Class<T> type) {
        this(type, null, true);
    }

    /**
     * Constructs instance for specified class type.
     *
     * @param type     class of the type
     * @param reusable <code>true</code> to reuse instance
     */
    public Implementor(@NonNull Class<T> type, boolean reusable) {
        this(type, null, reusable);
    }

    /**
     * Constructs object for specified class type.
     *
     * @param type     class of the type
     * @param loader   the class loader for load class
     * @param reusable <code>true</code> to reuse instance
     */
    public Implementor(@NonNull Class<T> type, ClassLoader loader, boolean reusable) {
        this.type = type;
        this.reusable = reusable;
        this.loader = loader;
    }

    /**
     * Registers new implementation with name and class path. <strong>NOTE:</strong> old implementation will be
     * overwritten
     *
     * @param name name of the implementation
     * @param path full class path of the implementation
     */
    public void register(String name, String path) {
        Validate.requireNotEmpty(name, "name cannot be null or empty");
        Validate.requireNotEmpty(path, "path cannot be null or empty");
        lock.lock();
        try {
            val imp = impHolders.get(name);
            if (imp != null) {
                imp.reset().path = path;
            } else {
                impHolders.put(name, new ImpHolder<T>(path));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Registers new implementation with name and class. <strong>NOTE:</strong> old implementation will be overwritten
     *
     * @param name  name of the implementation
     * @param clazz class of the implementation
     */
    public void register(String name, @NonNull Class<? extends T> clazz) {
        Validate.requireNotEmpty(name, "name cannot be null or empty");
        lock.lock();
        try {
            val imp = impHolders.get(name);
            if (imp != null) {
                imp.reset().clazz = clazz;
            } else {
                impHolders.put(name, new ImpHolder<>(clazz));
            }
        } finally {
            lock.unlock();
        }
    }

    public Set<String> names() {
        lock.lock();
        try {
            return Collections.unmodifiableSet(impHolders.keySet());
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(String name) {
        lock.lock();
        try {
            return impHolders.containsKey(name);
        } finally {
            lock.unlock();
        }
    }

    public void remove(String name) {
        lock.lock();
        try {
            impHolders.remove(name);
        } finally {
            lock.unlock();
        }
    }

    public T getInstance(@NonNull String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return getInstance(name, null);
    }

    /**
     * Returns an instance for specified implementation name.
     *
     * @param name name of the implementation
     * @return instance for the implementation, return {@literal null} if no implementation found
     * @throws IllegalAccessException if the class cannot access
     * @throws InstantiationException if the instance cannot be created
     * @throws ClassNotFoundException if the class path is invalid
     */
    public T getInstance(@NonNull String name, ClassLoader loader) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        lock.lock();
        try {
            val imp = impHolders.get(name);
            return imp != null ? imp.instantiate(type, loader != null ? loader : this.loader, reusable) : null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Loads registry from specified input.
     * <p>
     * The content of the input must be: [name]=[path to class].
     *
     * @param path   path to input
     * @param parser the parser for parse the value in each line
     */
    public void load(String path, BiFunction<String, String, String> parser) {
        lock.lock();
        try {
            load(path, loader, parser);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Loads registry from specified input.
     * <p>
     * The content of the input must be: [name]=[path to class].
     *
     * @param path   path to input
     * @param loader the class loader for load resources
     * @param parser the parser for parse the value in each line
     */
    public void load(String path, ClassLoader loader, BiFunction<String, String, String> parser) {
        lock.lock();
        try {
            val urls = IOUtils.resourcesFor(path, loader);
            if (urls == null) {
                return;
            }
            for (val url : urls) {
                try (val in = url.openStream()) {
                    val prop = new Properties();
                    prop.load(in);
                    for (val e : prop.entrySet()) {
                        val name = e.getKey().toString();
                        val value = e.getValue().toString();
                        register(name, parser == null ? value.trim() : parser.apply(name, value));
                    }
                } catch (IOException e) {
                    Log.e(TAG, e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private static class ImpHolder<T> {
        private String path;
        private T cache = null;
        private Class<? extends T> clazz;

        private ImpHolder(String path) {
            this.path = path;
        }

        private ImpHolder(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        private ImpHolder reset() {
            path = null;
            clazz = null;
            cache = null;
            return this;
        }

        /**
         * Creates a new instance of implement for <code>T</code>.
         *
         * @param loader the class loader
         * @return the new instance or <code>null</code> if class for path does not extends from <code>T</code>.
         * @throws ClassNotFoundException if the class of <code>path</code> is not found
         * @throws IllegalAccessException if the class of <code>path</code> is inaccessible
         * @throws InstantiationException if cannot create instance of the class
         */
        @SuppressWarnings("unchecked")
        private T instantiate(Class<T> type, ClassLoader loader, boolean reusable)
                throws ClassNotFoundException, IllegalAccessException, InstantiationException {
            if (reusable && cache != null) {
                return cache;
            }
            if (clazz == null) {
                Validate.checkNotNull(path, "No path and class specified");
                val klass = loader != null ? Class.forName(path, true, loader) : Class.forName(path);
                if (!type.isAssignableFrom(klass)) {
                    Log.d(TAG, "{1} not extend or implement {2}", klass.getName(), type.getName());
                    return null;
                }
                clazz = (Class<T>) klass;
            }
            val inst = clazz.newInstance();
            if (reusable) {
                cache = inst;
            }
            return inst;
        }
    }
}
