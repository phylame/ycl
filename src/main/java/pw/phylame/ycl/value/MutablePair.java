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

import lombok.val;

public class MutablePair<A, B> extends Pair<A, B> {
    private static final long serialVersionUID = -7871583557712920496L;

    public MutablePair() {

    }

    public MutablePair(A first, B second) {
        super(first, second);
    }

    public static <A, B> MutablePair<A, B> of(A first, B second) {
        return new MutablePair<>(first, second);
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public void set(A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public B setValue(B value) {
        val oldValue = second;
        second = value;
        return oldValue;
    }
}
