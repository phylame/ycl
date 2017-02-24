package pw.phylame.commons.cache;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.util.StringUtils;
import pw.phylame.commons.util.Validate;

public class LocalCache implements Cacheable {
    private volatile StringBuilder b = new StringBuilder();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Object add(@NonNull String text) {
        Validate.checkNotNull(b, "closed");
        if (text.isEmpty()) {
            return RangeTag.EMPTY;
        }
        val writeLock = lock.writeLock();
        writeLock.lock();
        try {
            val tag = new RangeTag(b.length(), text.length());
            b.append(text);
            return tag;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String get(Object tag) {
        Validate.checkNotNull(b, "closed");
        if (tag instanceof RangeTag) {
            val rt = (RangeTag) tag;
            if (rt.length == 0) {
                return StringUtils.EMPTY_TEXT;
            }
            val readLock = lock.readLock();
            readLock.lock();
            try {
                return b.substring((int) rt.offset, (int) rt.length);
            } finally {
                readLock.unlock();
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        val writeLock = lock.writeLock();
        writeLock.lock();
        try {
            b = null;
        } finally {
            writeLock.unlock();
        }
    }
}
