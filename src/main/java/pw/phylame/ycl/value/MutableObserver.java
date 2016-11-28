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

public class MutableObserver<T> extends Observer<T> implements MutableValue<T> {
    public MutableObserver(@NonNull MutableValue<T> value) {
        super(value);
    }

    @Override
    public final void set(T value) {
        onSetting(get(), value);
    }

    protected void onSetting(T oldValue, T newValue) {
        ((MutableValue<T>) this.value).set(newValue);
    }
}
