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
    public <X extends A, Y extends B> MutablePair(X first, Y second) {
        super(first, second);
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