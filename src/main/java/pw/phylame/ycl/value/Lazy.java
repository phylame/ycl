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

package pw.phylame.ycl.value;

import lombok.NonNull;
import pw.phylame.ycl.util.Log;
import pw.phylame.ycl.util.Provider;

public final class Lazy<T> {
    private volatile boolean initialized = false;

    private T value;

    private final Provider<T> provider;

    private final T fallback;

    public Lazy(Provider<T> provider) {
        this(provider, null);
    }

    public Lazy(@NonNull Provider<T> provider, T fallback) {
        this.provider = provider;
        this.fallback = fallback;
    }

    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        value = provider.provide();
                    } catch (Exception e) {
                        Log.d("Lazy", e);
                        value = fallback;
                    }
                    initialized = true;
                }
            }
        }
        return value;
    }
}
