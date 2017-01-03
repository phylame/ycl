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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferedRandomAccessFile extends RandomAccessFile {
    private static final int DEFAULT_BUFFER_BIT_LEN = 12;

    protected byte buf[];
    protected int bufbitlen;
    protected int bufsize;
    protected long bufmask;
    protected boolean bufdirty;
    protected int bufusedsize;
    protected long curpos;

    protected long bufstartpos;
    protected long bufendpos;
    protected long fileendpos;

    protected boolean append;
    protected String filename;
    protected long initfilelen;

    public BufferedRandomAccessFile(String name) throws IOException {
        this(name, "r", DEFAULT_BUFFER_BIT_LEN);
    }

    public BufferedRandomAccessFile(File file) throws IOException {
        this(file.getPath(), "r", DEFAULT_BUFFER_BIT_LEN);
    }

    public BufferedRandomAccessFile(String name, int bufbitlen) throws IOException {
        this(name, "r", bufbitlen);
    }

    public BufferedRandomAccessFile(File file, int bufbitlen) throws IOException {
        this(file.getPath(), "r", bufbitlen);
    }

    public BufferedRandomAccessFile(String name, String mode) throws IOException {
        this(name, mode, DEFAULT_BUFFER_BIT_LEN);
    }

    public BufferedRandomAccessFile(File file, String mode) throws IOException {
        this(file.getPath(), mode, DEFAULT_BUFFER_BIT_LEN);
    }

    public BufferedRandomAccessFile(String name, String mode, int bufbitlen) throws IOException {
        super(name, mode);
        init(name, mode, bufbitlen);
    }

    public BufferedRandomAccessFile(File file, String mode, int bufbitlen) throws IOException {
        this(file.getPath(), mode, bufbitlen);
    }

    private void init(String name, String mode, int bufbitlen) throws IOException {
        if (bufbitlen < 0) {
            throw new IllegalArgumentException("bufbitlen size must > 0");
        }

        append = !mode.equals("r");

        filename = name;
        initfilelen = super.length();
        fileendpos = initfilelen - 1;
        curpos = super.getFilePointer();

        this.bufbitlen = bufbitlen;
        bufsize = 1 << bufbitlen;
        buf = new byte[bufsize];
        bufmask = ~(bufsize - 1L);
        bufdirty = false;
        bufusedsize = 0;
        bufstartpos = -1;
        bufendpos = -1;
    }

    private void flushbuf() throws IOException {
        if (bufdirty) {
            if (super.getFilePointer() != bufstartpos) {
                super.seek(bufstartpos);
            }
            super.write(buf, 0, bufusedsize);
            bufdirty = false;
        }
    }

    private int fillbuf() throws IOException {
        super.seek(bufstartpos);
        bufdirty = false;
        return super.read(buf, 0, bufsize);
    }

    public int read(long pos) throws IOException {
        if (pos >= initfilelen) {
            return -1;
        }
        if (pos < bufstartpos || pos > bufendpos) {
            flushbuf();
            seek(pos);

            if (pos < bufstartpos || pos > bufendpos) {
                throw new IOException();
            }
        }
        curpos = pos;
        return buf[(int) (pos - bufstartpos)] & 0xFF;
    }

    public void append(byte bw) throws IOException {
        this.write(bw, fileendpos + 1);
    }

    public void write(byte bw, long pos) throws IOException {

        if (pos >= bufstartpos && pos <= bufendpos) { // write pos in buf
            buf[(int) (pos - bufstartpos)] = bw;
            bufdirty = true;

            if (pos == fileendpos + 1) { // write pos is append pos
                fileendpos++;
                bufusedsize++;
            }
        } else { // write pos not in buf
            seek(pos);

            if (pos >= 0 && pos <= fileendpos && fileendpos != 0) { // write pos is modify file
                buf[(int) (pos - bufstartpos)] = bw;

            } else if (pos == 0 && fileendpos == 0 || pos == fileendpos + 1) { // write pos is append
                // pos
                buf[0] = bw;
                fileendpos++;
                bufusedsize = 1;
            } else {
                throw new IndexOutOfBoundsException();
            }
            bufdirty = true;
        }
        curpos = pos;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {

        long writeendpos = curpos + len - 1;

        if (writeendpos <= bufendpos) { // b[] in cur buf
            System.arraycopy(b, off, buf, (int) (curpos - bufstartpos), len);
            bufdirty = true;
            bufusedsize = (int) (writeendpos - bufstartpos + 1);// (int)(this.curpos - this.bufstartpos + len
            // - 1);

        } else { // b[] not in cur buf
            super.seek(curpos);
            super.write(b, off, len);
        }

        if (writeendpos > fileendpos) {
            fileendpos = writeendpos;
        }

        seek(writeendpos + 1);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {

        long readendpos = curpos + len - 1;

        if (readendpos <= bufendpos && readendpos <= fileendpos) { // read in buf
            System.arraycopy(buf, (int) (curpos - bufstartpos), b, off, len);
        } else { // read b[] size > buf[]

            if (readendpos > fileendpos) { // read b[] part in file
                len = (int) (length() - curpos + 1);
            }

            super.seek(curpos);
            len = super.read(b, off, len);
            readendpos = curpos + len - 1;
        }
        seek(readendpos + 1);
        return len;
    }

    @Override
    public void write(byte b[]) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public int read(byte b[]) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public void write(int n) throws IOException {
        long pos = getFilePointer();
        this.write((byte) n, pos);
        seek(pos + 1);
    }

    @Override
    public int read() throws IOException {
        long pos = getFilePointer();
        int n = read(pos);
        seek(pos + 1);
        return n;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos < bufstartpos || pos > bufendpos) { // seek pos not in buf
            flushbuf();

            if (pos >= 0 && pos <= fileendpos && fileendpos != 0) { // seek pos in file (file length >
                // 0)
                bufstartpos = pos & bufmask;
                bufusedsize = fillbuf();

            } else if (pos == 0 && fileendpos == 0 || pos == fileendpos + 1) { // seek pos is append
                // pos

                bufstartpos = pos;
                bufusedsize = 0;
            }
            bufendpos = bufstartpos + bufsize - 1;
        }
        curpos = pos;
    }

    @Override
    public long length() throws IOException {
        return max(fileendpos + 1, initfilelen);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        if (newLength > 0) {
            fileendpos = newLength - 1;
        } else {
            fileendpos = 0;
        }
        super.setLength(newLength);
    }

    @Override
    public long getFilePointer() throws IOException {
        return curpos;
    }

    private long max(long a, long b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    @Override
    public void close() throws IOException {
        flushbuf();
        super.close();
    }
}
