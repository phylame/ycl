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

package pw.phylame.commons.io;

import lombok.val;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for LZ77.
 */
public final class Lz77Utils {
    private Lz77Utils() {
    }

    public static ByteBuilder decompress(InputStream in) throws IOException {
        int ch;
        val out = new ByteBuilder();
        byte[] buf = new byte[8];
        while ((ch = in.read()) != -1) {
            if (ch == 0) {
                out.append(ch);
            } else if (ch >= 1 && ch <= 8) {
                out.append(buf = readBytes(in, buf, ch), 0, ch);
            } else if (ch <= 0x7F) {
                out.append(ch);
            } else if (ch >= 0xC0) {
                out.append(' ').append(ch ^ 0x80);
            } else {
                ch = (ch << 8) + (readByte(in) & 0xFF);
                int di = (ch & 0x3FFF) >> 3;
                val b = out.getDirectArray();
                for (int n = (ch & 7) + 3; n-- != 0; ) {
                    out.append(b[out.size() - di]);
                }
            }
        }
        return out;
    }

    private static int readByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        } else {
            return b;
        }
    }

    private static byte[] readBytes(InputStream in, byte[] buf, int length) throws IOException {
        if (in.read(buf, 0, length) != length) {
            throw new EOFException();
        }
        return buf;
    }
}
