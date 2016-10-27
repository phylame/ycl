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

import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public final class Pair<A, B> implements Map.Entry<A, B> {
    private A first;

    private B second;

    public <X extends A, Y extends B> Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final A getKey() {
        return first;
    }

    @Override
    public final B getValue() {
        return second;
    }

    @Override
    public B setValue(B value) {
        throw new UnsupportedOperationException();
    }
}
