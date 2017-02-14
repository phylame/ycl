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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.*;

/**
 * Utility class for ZLib operations.
 */
public final class ZLibUtils {
    private ZLibUtils() {
    }

    /**
     * Buffer size
     */
    public static final int BUFFER_SIZE = 2048;

    private static boolean isEmpty(byte[] b) {
        return b == null || b.length == 0;
    }

    /**
     * Compresses specified byte data with default compression level.
     *
     * @param data the input byte data to be compressed
     * @return compressed data
     */
    public static byte[] compress(byte[] data) {
        return compress(data, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Compresses specified input data with specified compression level.
     *
     * @param data  the input byte data to be compressed
     * @param level ZLIB compression level
     * @return compressed data
     */
    public static byte[] compress(byte[] data, int level) {
        return compress(data, 0, data.length, level);
    }

    /**
     * Compresses a specified area of input byte data with default compression level.
     *
     * @param data   the input byte data
     * @param offset start index of compressing area
     * @param length length of compressing area
     * @return compressed data
     */
    public static byte[] compress(byte[] data, int offset, int length) {
        return compress(data, offset, length, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Compresses a specified area of input byte data with specified compression level.
     *
     * @param data   the input byte data
     * @param offset start index of compressing area
     * @param length length of compressing area
     * @param level  ZLIB compression level
     * @return compressed data
     */
    public static byte[] compress(byte[] data, int offset, int length, int level) {
        if (isEmpty(data)) {
            return data;
        }
        val compresser = new Deflater(level);
        compresser.reset();
        compresser.setInput(data, offset, length);
        compresser.finish();
        val baos = new ByteArrayOutputStream(length);
        val buf = new byte[BUFFER_SIZE];
        while (!compresser.finished()) {
            baos.write(buf, 0, compresser.deflate(buf));
        }
        compresser.end();
        return baos.toByteArray();
    }

    /**
     * Compresses specified input data with default compression level
     * and writes to output.
     *
     * @param data   the input byte data to be compressed
     * @param output the output stream
     * @throws IOException if occur IO errors
     */
    public static void compress(byte[] data, OutputStream output) throws IOException {
        compress(data, 0, data.length, output);
    }

    /**
     * Compresses a specified area of input data with default compression level
     * and writes to output.
     *
     * @param data   the input byte data
     * @param offset start index of compressing area
     * @param length length of compression area
     * @param output the output stream
     * @throws IOException if occur IO errors
     */
    public static void compress(byte[] data, int offset, int length, OutputStream output) throws IOException {
        val dos = new DeflaterOutputStream(output);
        dos.write(data, offset, length);
        dos.finish();
        dos.flush();
    }

    /**
     * Decompresses specified input byte data.
     *
     * @param data the input byte data to be decompressed
     * @return decompressed data
     * @throws DataFormatException if the compressed data format is invalid
     */
    public static byte[] decompress(byte[] data) throws DataFormatException {
        return decompress(data, 0, data.length);
    }

    /**
     * Decompresses a specified area of input data.
     *
     * @param data   the input byte data
     * @param offset start index of decompressing area
     * @param length length of decompression area
     * @return decompressed data
     * @throws DataFormatException if the compressed data format is invalid
     */
    public static byte[] decompress(byte[] data, int offset, int length) throws DataFormatException {
        if (isEmpty(data)) {
            return data;
        }
        val decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data, offset, length);
        val baos = new ByteArrayOutputStream(length);
        byte[] buf = new byte[BUFFER_SIZE];
        while (!decompresser.finished()) {
            baos.write(buf, 0, decompresser.inflate(buf));
        }
        decompresser.end();
        return baos.toByteArray();
    }

    /**
     * Decompresses byte data from specified input stream.
     *
     * @param input the input stream
     * @return decompressed data
     * @throws IOException if an I/O error occurs
     */
    public static byte[] decompress(InputStream input) throws IOException {
        val iis = new InflaterInputStream(input);
        val baos = new ByteArrayOutputStream(BUFFER_SIZE);
        val buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = iis.read(buf)) > 0) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }
}
