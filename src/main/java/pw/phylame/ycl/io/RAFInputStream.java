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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Wrapper for block of <code>RandomAccessFile</code> as <code>InputStream</code>.
 */
public class RAFInputStream extends InputStream {
    private final RandomAccessFile source;
    private final long endPos;    // value: curPos + size
    private long curPos;

    public RAFInputStream(RandomAccessFile source, long size) throws IOException {
        this(source, source.getFilePointer(), size);
    }

    // size < 0 to use all content of source
    public RAFInputStream(RandomAccessFile source, long offset, long size) throws IOException {
        if (source == null) {
            throw new NullPointerException("source");
        }

        this.source = source;
        long length = source.length();

        curPos = (offset < 0) ? 0 : offset;
        endPos = (size < 0) ? length : curPos + size;

        if (curPos >= length) {
            throw new IllegalArgumentException("offset >= length of source");
        }
        if (endPos > length) {
            throw new IllegalArgumentException("offset + size > length of source");
        }
    }

    @Override
    public int read() throws IOException {
        if (curPos < endPos) {
            ++curPos;
            return source.read();
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        long count = endPos - curPos;
        if (count == 0) {
            return -1;
        }
        count = count < len ? count : len;
        len = source.read(b, off, (int) count);
        curPos += count;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }
        n = source.skipBytes((int) Math.min(n, endPos - curPos));
        curPos = Math.min(curPos + n, endPos);
        return n;
    }

    @Override
    public int available() throws IOException {
        return (int) (endPos - curPos);
    }
}
