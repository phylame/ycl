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
        this.init(name, mode, bufbitlen);
    }

    public BufferedRandomAccessFile(File file, String mode, int bufbitlen) throws IOException {
        this(file.getPath(), mode, bufbitlen);
    }

    private void init(String name, String mode, int bufbitlen) throws IOException {
        if (bufbitlen < 0) {
            throw new IllegalArgumentException("bufbitlen size must > 0");
        }

        this.append = !mode.equals("r");

        this.filename = name;
        this.initfilelen = super.length();
        this.fileendpos = this.initfilelen - 1;
        this.curpos = super.getFilePointer();

        this.bufbitlen = bufbitlen;
        this.bufsize = 1 << bufbitlen;
        this.buf = new byte[this.bufsize];
        this.bufmask = ~((long) this.bufsize - 1L);
        this.bufdirty = false;
        this.bufusedsize = 0;
        this.bufstartpos = -1;
        this.bufendpos = -1;
    }

    private void flushbuf() throws IOException {
        if (this.bufdirty) {
            if (super.getFilePointer() != this.bufstartpos) {
                super.seek(this.bufstartpos);
            }
            super.write(this.buf, 0, this.bufusedsize);
            this.bufdirty = false;
        }
    }

    private int fillbuf() throws IOException {
        super.seek(this.bufstartpos);
        this.bufdirty = false;
        return super.read(this.buf, 0, this.bufsize);
    }

    public byte read(long pos) throws IOException {
        if (pos >= this.initfilelen) {
            return -1;
        }
        if (pos < this.bufstartpos || pos > this.bufendpos) {
            this.flushbuf();
            this.seek(pos);

            if ((pos < this.bufstartpos) || (pos > this.bufendpos)) {
                throw new IOException();
            }
        }
        this.curpos = pos;
        return this.buf[(int) (pos - this.bufstartpos)];
    }

    public void append(byte bw) throws IOException {
        this.write(bw, this.fileendpos + 1);
    }

    public void write(byte bw, long pos) throws IOException {

        if ((pos >= this.bufstartpos) && (pos <= this.bufendpos)) { // write pos in buf
            this.buf[(int) (pos - this.bufstartpos)] = bw;
            this.bufdirty = true;

            if (pos == this.fileendpos + 1) { // write pos is append pos
                this.fileendpos++;
                this.bufusedsize++;
            }
        } else { // write pos not in buf
            this.seek(pos);

            if ((pos >= 0) && (pos <= this.fileendpos) && (this.fileendpos != 0)) { // write pos is modify file
                this.buf[(int) (pos - this.bufstartpos)] = bw;

            } else if (((pos == 0) && (this.fileendpos == 0)) || (pos == this.fileendpos + 1)) { // write pos is append pos
                this.buf[0] = bw;
                this.fileendpos++;
                this.bufusedsize = 1;
            } else {
                throw new IndexOutOfBoundsException();
            }
            this.bufdirty = true;
        }
        this.curpos = pos;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {

        long writeendpos = this.curpos + len - 1;

        if (writeendpos <= this.bufendpos) { // b[] in cur buf
            System.arraycopy(b, off, this.buf, (int) (this.curpos - this.bufstartpos), len);
            this.bufdirty = true;
            this.bufusedsize = (int) (writeendpos - this.bufstartpos + 1);//(int)(this.curpos - this.bufstartpos + len - 1);

        } else { // b[] not in cur buf
            super.seek(this.curpos);
            super.write(b, off, len);
        }

        if (writeendpos > this.fileendpos)
            this.fileendpos = writeendpos;

        this.seek(writeendpos + 1);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {

        long readendpos = this.curpos + len - 1;

        if (readendpos <= this.bufendpos && readendpos <= this.fileendpos) { // read in buf
            System.arraycopy(this.buf, (int) (this.curpos - this.bufstartpos), b, off, len);
        } else { // read b[] size > buf[]

            if (readendpos > this.fileendpos) { // read b[] part in file
                len = (int) (this.length() - this.curpos + 1);
            }

            super.seek(this.curpos);
            len = super.read(b, off, len);
            readendpos = this.curpos + len - 1;
        }
        this.seek(readendpos + 1);
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
        if ((pos < this.bufstartpos) || (pos > this.bufendpos)) { // seek pos not in buf
            this.flushbuf();

            if ((pos >= 0) && (pos <= this.fileendpos) && (this.fileendpos != 0)) { // seek pos in file (file length > 0)
                this.bufstartpos = pos & this.bufmask;
                this.bufusedsize = this.fillbuf();

            } else if (((pos == 0) && (this.fileendpos == 0)) || (pos == this.fileendpos + 1)) { // seek pos is append pos

                this.bufstartpos = pos;
                this.bufusedsize = 0;
            }
            this.bufendpos = this.bufstartpos + this.bufsize - 1;
        }
        this.curpos = pos;
    }

    @Override
    public long length() throws IOException {
        return this.max(this.fileendpos + 1, this.initfilelen);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        if (newLength > 0) {
            this.fileendpos = newLength - 1;
        } else {
            this.fileendpos = 0;
        }
        super.setLength(newLength);
    }

    @Override
    public long getFilePointer() throws IOException {
        return this.curpos;
    }

    private long max(long a, long b) {
        if (a > b) return a;
        return b;
    }

    @Override
    public void close() throws IOException {
        this.flushbuf();
        super.close();
    }
}
