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

public class MutableTriple<A, B, C> extends Triple<A, B, C> {
    private static final long serialVersionUID = 767137123548967736L;

    public MutableTriple() {
    }

    public <F extends A, S extends B, T extends C> MutableTriple(F first, S second, T third) {
        super(first, second, third);
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public void setThird(C third) {
        this.third = third;
    }

    public void set(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
