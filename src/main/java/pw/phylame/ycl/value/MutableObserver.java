/*
 * Copyright 2017 Peng Wan <phylame@163.com>
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
import pw.phylame.ycl.util.Function;

public class MutableObserver<T> extends Observer<T> implements MutableValue<T> {
    private final Function<T, T> setObserver;

    public MutableObserver(MutableValue<T> value, Function<T, T> getObserver, @NonNull Function<T, T> setObserver) {
        super(value, getObserver);
        this.setObserver = setObserver;
    }

    @Override
    public final void set(T value) {
        ((MutableValue<T>) this.value).set(setObserver.apply(value));
    }
}
