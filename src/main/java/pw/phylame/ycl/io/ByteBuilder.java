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

package pw.phylame.ycl.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.NonNull;
import lombok.SneakyThrows;

public class ByteBuilder extends ByteArrayOutputStream {

    public ByteBuilder() {
        super();
    }

    public ByteBuilder(int size) {
        super(size);
    }

    public ByteBuilder(@NonNull byte[] buf) {
        this.buf = buf;
    }

    public ByteBuilder append(int b) {
        write(b);
        return this;
    }

    @SneakyThrows(IOException.class)
    public ByteBuilder append(byte[] b) {
        write(b);
        return this;
    }

    public ByteBuilder append(byte[] b, int off, int len) {
        write(b, off, len);
        return this;
    }

    public byte[] getDirectArray() {
        return buf;
    }

}
