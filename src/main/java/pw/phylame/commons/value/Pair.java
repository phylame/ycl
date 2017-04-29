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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class Pair<A, B> implements Value<B>, Map.Entry<A, B>, Serializable {
    private static final long serialVersionUID = -5592516496835082752L;

    protected A first;

    protected B second;

    public Pair() {
    }

    public <F extends A, S extends B> Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final B get() {
        return second;
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
