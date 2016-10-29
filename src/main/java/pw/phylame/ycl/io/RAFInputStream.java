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

package pw.phylame.ycl.io;

import lombok.NonNull;
import pw.phylame.ycl.util.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Wrapper for block of <code>RandomAccessFile</code> as <code>InputStream</code>.
 */
public class RAFInputStream extends InputStream {
    private final RandomAccessFile source;
    private final long endpos; // value: curpos + size
    private long curpos;

    public RAFInputStream(RandomAccessFile source, long size) throws IOException {
        this(source, source.getFilePointer(), size);
    }

    // size < 0 to use all content of source
    public RAFInputStream(@NonNull RandomAccessFile source, long offset, long size) throws IOException {
        this.source = source;
        long length = source.length();

        curpos = (offset < 0) ? 0 : offset;
        endpos = (size < 0) ? length : curpos + size;

        Validate.require(curpos < length, "offset >= length of source");
        Validate.require(endpos <= length, "offset + size > length of source");
    }

    @Override
    public int read() throws IOException {
        if (curpos < endpos) {
            ++curpos;
            return source.read();
        } else {
            return -1;
        }
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        long count = endpos - curpos;
        if (count == 0) {
            return -1;
        }
        count = count < len ? count : len;
        len = source.read(b, off, (int) count);
        curpos += count;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }
        n = source.skipBytes((int) Math.min(n, endpos - curpos));
        curpos = Math.min(curpos + n, endpos);
        return n;
    }

    @Override
    public int available() throws IOException {
        return (int) (endpos - curpos);
    }
}
