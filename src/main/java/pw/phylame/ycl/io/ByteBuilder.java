package pw.phylame.ycl.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.NonNull;
import lombok.SneakyThrows;

public class ByteBuilder extends ByteArrayOutputStream {

    public ByteBuilder() {
        super();
    }

    public ByteBuilder(int size) {
        super(size);
    }

    public ByteBuilder(@NonNull byte[] buf) {
        this.buf = buf;
    }

    public ByteBuilder append(int b) {
        write(b);
        return this;
    }

    @SneakyThrows(IOException.class)
    public ByteBuilder append(byte[] b) {
        write(b);
        return this;
    }

    public ByteBuilder append(byte[] b, int off, int len) {
        write(b, off, len);
        return this;
    }

    public byte[] getDirectArray() {
        return buf;
    }

}
