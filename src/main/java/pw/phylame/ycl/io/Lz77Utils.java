package pw.phylame.ycl.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import lombok.val;

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
                for (int n = (ch & 7) + 3; n-- != 0;) {
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
