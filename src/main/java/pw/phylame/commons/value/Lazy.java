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

package pw.phylame.commons.value;

import lombok.Getter;
import lombok.NonNull;
import pw.phylame.commons.function.Provider;

public class Lazy<T> implements Value<T> {
    @Getter
    protected volatile boolean initialized = false;

    private final Provider<? extends T> provider;

    @Getter
    private final T fallback;

    @Getter
    protected Exception error;

    protected T value;

    public Lazy(Provider<? extends T> provider) {
        this(provider, null);
    }

    public Lazy(@NonNull Provider<? extends T> provider, T fallback) {
        this.provider = provider;
        this.fallback = fallback;
    }

    @Override
    public final T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        value = provider.provide();
                    } catch (Exception e) {
                        value = fallback;
                        error = e;
                    }
                    initialized = true;
                }
            }
        }
        return value;
    }
}
