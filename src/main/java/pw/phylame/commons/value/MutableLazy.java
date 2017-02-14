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

import pw.phylame.commons.function.Provider;

public class MutableLazy<T> extends Lazy<T> implements MutableValue<T> {
    public MutableLazy(Provider<? extends T> provider) {
        super(provider);
    }

    public MutableLazy(Provider<? extends T> provider, T fallback) {
        super(provider, fallback);
    }

    @Override
    public void set(T value) {
        this.value = value;
        initialized = true;
    }
}
