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

import lombok.val;

public class MutablePair<A, B> extends Pair<A, B> implements MutableValue<B> {
    private static final long serialVersionUID = -7871583557712920496L;

    public MutablePair() {
    }

    public <F extends A, S extends B> MutablePair(F first, S second) {
        super(first, second);
    }

    public final void setFirst(A first) {
        this.first = first;
    }

    public final void setSecond(B second) {
        this.second = second;
    }

    public final void set(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final B setValue(B value) {
        val oldValue = second;
        second = value;
        return oldValue;
    }

    @Override
    public final B set(B value) {
        return setValue(value);
    }
}
