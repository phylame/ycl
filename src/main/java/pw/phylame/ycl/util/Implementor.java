/*
 * Copyright 2016 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package pw.phylame.ycl.util;

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.log.Log;

import java.util.HashMap;
import java.util.Map;

public class Implementor<T> {
    private static final String TAG = "IMP";

    private final Class<T> type;
    private final boolean reusable;
    private final ClassLoader loader;

    private final Map<String, ImpHolder> impHolders = new HashMap<>();

    public Implementor(@NonNull Class<T> type, boolean reusable) {
        this(type, reusable, null);
    }

    /**
     * Constructs object with specified class type.
     *
     * @param type
     *            class of the interface
     * @param reusable
     *            <code>true</code> to reuse instance
     */
    public Implementor(@NonNull Class<T> type, boolean reusable, ClassLoader loader) {
        this.type = type;
        this.reusable = reusable;
        this.loader = loader;
    }

    /**
     * Registers new implementation with name and class path. <strong>NOTE:</strong> old implementation will be
     * overwritten
     *
     * @param name
     *            name of the implementation
     * @param path
     *            full class path of the implementation
     */
    public void register(String name, String path) {
        Validate.require(name != null && !name.isEmpty(), "name cannot be null or empty");
        Validate.require(path != null && !path.isEmpty(), "path cannot be null or empty");
        synchronized (this) {
            val imp = impHolders.get(name);
            if (imp != null) {
                imp.reset().path = path;
            } else {
                impHolders.put(name, new ImpHolder(path));
            }
        }
    }

    /**
     * Registers new implementation with name and class. <strong>NOTE:</strong> old implementation will be overwritten
     *
     * @param name
     *            name of the implementation
     * @param clazz
     *            class of the implementation
     */
    public void register(String name, @NonNull Class<? extends T> clazz) {
        Validate.require(name != null && !name.isEmpty(), "name cannot be null or empty");
        synchronized (this) {
            val imp = impHolders.get(name);
            if (imp != null) {
                imp.reset().clazz = clazz;
            } else {
                impHolders.put(name, new ImpHolder(clazz));
            }
        }
    }

    public String[] names() {
        synchronized (this) {
            return impHolders.keySet().toArray(new String[impHolders.size()]);
        }
    }

    public boolean contains(String name) {
        synchronized (this) {
            return impHolders.containsKey(name);
        }
    }

    public void remove(String name) {
        synchronized (this) {
            impHolders.remove(name);
        }
    }

    /**
     * Returns an instance for specified implementation name.
     *
     * @param name
     *            name of the implementation
     * @return instance for the implementation
     * @throws IllegalAccessException
     *             if the class cannot access
     * @throws InstantiationException
     *             if the instance cannot be created
     * @throws ClassNotFoundException
     *             if the class path is invalid
     */
    public T getInstance(@NonNull String name)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        synchronized (this) {
            val imp = impHolders.get(name);
            return imp != null ? imp.instantiate() : null;
        }
    }

    private class ImpHolder {
        private String path;
        private Class<? extends T> clazz;
        private T cache = null;

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
         * @return the new instance or <code>null</code> if class for path does not extends from <code>T</code>.
         * @throws ClassNotFoundException
         *             if the class of <code>path</code> is not found
         * @throws IllegalAccessException
         *             if the class of <code>path</code> is inaccessible
         * @throws InstantiationException
         *             if cannot create instance of the class
         */
        @SuppressWarnings("unchecked")
        private T instantiate() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
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
