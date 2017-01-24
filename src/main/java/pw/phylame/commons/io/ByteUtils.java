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

package pw.phylame.commons.io;

import lombok.NonNull;
import lombok.Value;

/**
 * Utility class for byte operations.
 * <p>
 * This class contains a number of static methods perform byte operations.
 */
public final class ByteUtils {

    private ByteUtils() {
    }

    /**
     * Byte endian
     */
    public enum Endian {
        BIG, LITTLE
    }

    public static final Endian defaultEndian = Endian.BIG;

    public static byte[] putInt8(byte x) {
        return putUInt8(x);
    }

    public static byte[] putUInt8(int x) {
        byte[] ret = new byte[1];
        ret[0] = (byte) x;
        return ret;
    }

    public static byte[] putInt16(short x) {
        return putUInt16(x, defaultEndian);
    }

    public static byte[] putInt16(short x, Endian endian) {
        return putUInt16(x, endian);
    }

    public static byte[] putUInt16(int x) {
        return putUInt16(x, defaultEndian);
    }

    public static byte[] putUInt16(int x, Endian endian) {
        byte[] ret = new byte[2];
        if (endian == Endian.BIG) {
            ret[0] = (byte) (x >> 8);
            ret[1] = (byte) (x);
        } else {
            ret[1] = (byte) (x >> 8);
            ret[0] = (byte) x;
        }
        return ret;
    }

    public static byte[] putInt32(int x) {
        return putUInt32(x, defaultEndian);
    }

    public static byte[] putInt32(int x, Endian endian) {
        return putUInt32(x, endian);
    }

    public static byte[] putUInt32(long x) {
        return putUInt32(x, defaultEndian);
    }

    public static byte[] putUInt32(long x, Endian endian) {
        byte[] ret = new byte[4];
        if (endian == Endian.BIG) {
            ret[0] = (byte) (x >> 24);
            ret[1] = (byte) (x >> 16);
            ret[2] = (byte) (x >> 8);
            ret[3] = (byte) x;
        } else {
            ret[3] = (byte) (x >> 24);
            ret[2] = (byte) (x >> 16);
            ret[1] = (byte) (x >> 8);
            ret[0] = (byte) x;
        }
        return ret;
    }

    public static byte getInt8(byte[] b, int index) {
        return b[index];
    }

    public static int getUInt8(byte[] b, int index) {
        return b[index] & 0xFF;
    }

    public static short getInt16(byte[] b, int index) {
        return getInt16(b, index, defaultEndian);
    }

    public static short getInt16(byte[] b, int index, Endian endian) {
        if (endian == Endian.BIG) {
            return (short) ((b[index] << 8) | (b[1 + index] & 0xFF));
        } else {
            return (short) ((b[1 + index] << 8) | (b[index] & 0xFF));
        }
    }

    public static int getUInt16(byte[] b, int index) {
        return getUInt16(b, index, defaultEndian);
    }

    public static int getUInt16(byte[] b, int index, Endian endian) {
        return getInt16(b, index, endian) & 0xFFFF;
    }

    public static int getInt32(byte[] b, int index) {
        return getInt32(b, index, defaultEndian);
    }

    public static int getInt32(byte[] b, int index, Endian endian) {
        if (endian == Endian.LITTLE) {
            return ((b[3 + index] & 0xFF) << 24)
                    | ((b[2 + index] & 0xFF) << 16)
                    | ((b[1 + index] & 0xFF) << 8)
                    | (b[index] & 0xFF);
        } else {
            return ((b[index] & 0xFF) << 24)
                    | ((b[1 + index] & 0xFF) << 16)
                    | ((b[2 + index] & 0xFF) << 8)
                    | (b[3 + index] & 0xFF);
        }
    }

    public static long getUInt32(byte[] b, int index) {
        return getInt32(b, index, defaultEndian);
    }

    public static long getUInt32(byte[] b, int index, Endian endian) {
        return getInt32(b, index, endian) & 0xFFFFFFFFL;
    }

    public static int getUnsignedByte(byte n) {
        return n & 0xFF;
    }

    public static int getUnsignedShort(short n) {
        return n & 0xFFFF;
    }

    public static long getUnsignedInt(int n) {
        return n & 0xFFFFFFFFL;
    }

    public static byte[] putShort(short x) {
        return putInt16(x, defaultEndian);
    }

    public static short getShort(byte[] b, int index) {
        return getInt16(b, index, defaultEndian);
    }

    public static byte[] putInt(int x) {
        return putInt32(x, defaultEndian);
    }

    public static int getInt(byte[] b, int index) {
        return getInt32(b, index, defaultEndian);
    }

    public static byte[] putLong(long x) {
        byte[] b = new byte[8];
        putLong(x, b, 0);
        return b;
    }

    public static void putLong(long x, byte[] b, int index) {
        b[index] = (byte) (x >> 56);
        b[index + 1] = (byte) (x >> 48);
        b[index + 2] = (byte) (x >> 40);
        b[index + 3] = (byte) (x >> 32);
        b[index + 4] = (byte) (x >> 24);
        b[index + 5] = (byte) (x >> 16);
        b[index + 6] = (byte) (x >> 8);
        b[index + 7] = (byte) x;
    }

    public static long getLong(byte[] b, int index) {
        return ((((long) b[index + 7] & 0xFF) << 56)
                | (((long) b[index + 6] & 0xFF) << 48)
                | (((long) b[index + 5] & 0xFF) << 40)
                | (((long) b[index + 4] & 0xFF) << 32)
                | (((long) b[index + 3] & 0xFF) << 24)
                | (((long) b[index + 2] & 0xFF) << 16)
                | (((long) b[index + 1] & 0xFF) << 8)
                | ((long) b[index] & 0xFF));
    }

    public static byte[] putChar(char x) {
        byte[] b = new byte[2];
        putChar(x, b, 0);
        return b;
    }

    public static void putChar(char ch, byte[] b, int index) {
        int temp = ch;
        for (int i = 0; i < 2; i++) {
            b[index + i] = new Integer(temp & 0xFF).byteValue();
            temp = temp >> 8;
        }
    }

    public static char getChar(byte[] b, int index) {
        int s = 0;
        if (b[index + 1] > 0)
            s += b[index + 1];
        else
            s += 256 + b[index];
        s <<= 8; // * 256
        if (b[index] > 0)
            s += b[index + 1];
        else
            s += 256 + b[index];
        return (char) s;
    }

    public static byte[] putFloat(float x) {
        byte[] b = new byte[4];
        putFloat(x, b, 0);
        return b;
    }

    public static void putFloat(float x, byte[] b, int index) {
        int n = Float.floatToIntBits(x);
        for (int i = 3; i >= 0; i++) {
            b[index + i] = new Integer(n).byteValue();
            n = n >> 8;
        }
    }

    public static float getFloat(byte[] b, int index) {
        int n;
        n = b[index + 3];
        n &= 0xFF;
        n |= ((long) b[index + 2] << 8);
        n &= 0xFFFF;
        n |= ((long) b[index + 1] << 16);
        n &= 0xFFFFFF;
        n |= ((long) b[index] << 24);
        return Float.intBitsToFloat(n);
    }

    public static byte[] putDouble(double x) {
        byte[] b = new byte[8];
        putDouble(x, b, 0);
        return b;
    }

    public static void putDouble(double x, byte[] b, int index) {
        long n = Double.doubleToLongBits(x);
        for (int i = 7; i >= 0; i++) {
            b[index + i] = new Long(n).byteValue();
            n = n >> 8;
        }
    }

    public static double getDouble(byte[] b, int index) {
        long n;
        n = b[index + 7];
        n &= 0xFF;
        n |= ((long) b[index + 6] << 8);
        n &= 0xFFFF;
        n |= ((long) b[index + 5] << 16);
        n &= 0xFFFFFF;
        n |= ((long) b[index + 4] << 24);
        n &= 0xFFFFFFFFL;
        n |= ((long) b[index + 3] << 32);
        n &= 0xFFFFFFFFFFL;
        n |= ((long) b[index + 2] << 40);
        n &= 0xFFFFFFFFFFFFL;
        n |= ((long) b[index + 1] << 48);
        n &= 0xFFFFFFFFFFFFFFL;
        n |= ((long) b[index] << 56);
        return Double.longBitsToDouble(n);
    }

    /**
     * Inner type to bytes.
     */
    @Value
    public static final class Render {
        @NonNull
        private Endian endian;

        public byte[] putInt8(byte x) {
            return ByteUtils.putInt8(x);
        }

        public byte[] putUInt8(int x) {
            return ByteUtils.putUInt8(x);
        }

        public byte[] putInt16(short x) {
            return ByteUtils.putInt16(x, endian);
        }

        public byte[] putUInt16(int x) {
            return ByteUtils.putUInt16(x, endian);
        }

        public byte[] putInt32(int x) {
            return ByteUtils.putInt32(x, endian);
        }

        public byte[] putUInt32(long x) {
            return ByteUtils.putUInt32(x, endian);
        }
    }

    public static final Render bigRender = new Render(Endian.BIG);
    public static final Render littleRender = new Render(Endian.LITTLE);

    /**
     * Bytes to inner type.
     */
    @Value
    public static final class Parser {
        @NonNull
        private Endian endian;

        public byte getInt8(byte[] b, int index) {
            return ByteUtils.getInt8(b, index);
        }

        public int getUInt8(byte[] b, int index) {
            return ByteUtils.getUInt8(b, index);
        }

        public short getInt16(byte[] b, int index) {
            return ByteUtils.getInt16(b, index, endian);
        }

        public int getUInt16(byte[] b, int index) {
            return ByteUtils.getUInt16(b, index, endian);
        }

        public int getInt32(byte[] b, int index) {
            return ByteUtils.getInt32(b, index, endian);
        }

        public long getUInt32(byte[] b, int index) {
            return ByteUtils.getUInt32(b, index, endian);
        }
    }

    public static final Parser bigParser = new Parser(Endian.BIG);
    public static final Parser littleParser = new Parser(Endian.LITTLE);
}
