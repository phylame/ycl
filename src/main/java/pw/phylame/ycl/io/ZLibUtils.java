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
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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
        byte[] output = new byte[0];
        Deflater compresser = new Deflater(level);
        compresser.reset();
        compresser.setInput(data, offset, length);
        compresser.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                baos.write(buf, 0, i);
            }
            output = baos.toByteArray();
        } catch (Exception e) {
            output = Arrays.copyOfRange(data, offset, offset + length);
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    /**
     * Compresses specified input data with default compression level
     * and writes to output.
     *
     * @param data   the input byte data to be compressed
     * @param output the output stream
     */
    public static void compress(byte[] data, OutputStream output) {
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
     */
    public static void compress(byte[] data, int offset, int length, OutputStream output) {
        DeflaterOutputStream dos = new DeflaterOutputStream(output);
        try {
            dos.write(data, offset, length);
            dos.finish();
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decompresses specified input byte data.
     *
     * @param data the input byte data to be decompressed
     * @return decompressed data
     */
    public static byte[] decompress(byte[] data) {
        return decompress(data, 0, data.length);
    }

    /**
     * Decompresses a specified area of input data.
     *
     * @param data   the input byte data
     * @param offset start index of decompressing area
     * @param length length of decompression area
     * @return decompressed data
     */
    public static byte[] decompress(byte[] data, int offset, int length) {
        byte[] output = new byte[0];
        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data, offset, length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                baos.write(buf, 0, i);
            }
            output = baos.toByteArray();
        } catch (Exception e) {
            output = Arrays.copyOfRange(data, offset, offset + length);
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }

    /**
     * Decompresses byte data from specified input stream.
     *
     * @param input the input stream
     * @return decompressed data
     */
    public static byte[] decompress(InputStream input) {
        InflaterInputStream iis = new InflaterInputStream(input);
        ByteArrayOutputStream o = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            int i = BUFFER_SIZE;
            byte[] buf = new byte[i];

            while ((i = iis.read(buf, 0, i)) > 0) {
                o.write(buf, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o.toByteArray();
    }
}
