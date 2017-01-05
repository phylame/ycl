package pw.phylame.ycl.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import lombok.val;

/**
 * Utilities for LZ77.
 */
public final class LZ77Utils {
    private LZ77Utils() {
    }

    public static ByteBuilder uncompress(InputStream in) throws IOException {
        int ch;
        @SuppressWarnings("resource")
        val out = new ByteBuilder();
        byte[] buf = new byte[8];
        while ((ch = readByte(in)) != -1) {
            if (ch == 0) {
                out.write(ch);
            } else if (ch <= 8) {
                out.write(buf = readBytes(in, buf, ch), 0, ch);
            } else if (ch <= 0x7f) {
                out.write(ch);
            } else if (ch <= 0xbf) {
                int lz77 = ByteUtils.getUInt16(new byte[]{(byte) ch, (byte) readByte(in)}, 0);
                lz77 &= 0x3fff;
                int length = (lz77 & 0x0007) + 3;
                int offset = lz77 >> 3;
                if (offset < 1) {
                    break;
//                    throw new IllegalStateException("Bad LZ77 data");
                }
                for (int i = 0; i < length; ++i) {
                    int pos = out.size() - offset;
                    if (pos < 0) {
                        throw new IllegalStateException("Bad LZ77 data");
                    }
                    out.write(out.getDirectArray()[pos]);
                }
            } else {
                out.append(' ').append(ch ^ 0x80);
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
