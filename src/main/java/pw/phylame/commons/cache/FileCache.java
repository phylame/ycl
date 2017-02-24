package pw.phylame.commons.cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.function.Provider;
import pw.phylame.commons.log.Log;
import pw.phylame.commons.util.StringUtils;
import pw.phylame.commons.util.Validate;
import pw.phylame.commons.value.Lazy;

public class FileCache implements Cacheable {
    private static final String TAG = FileCache.class.getSimpleName();
    private static final Charset ENCODING = Charset.forName("UTF-16BE");

    private File cache;
    private volatile boolean closed = false;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lazy<RandomAccessFile> raf = new Lazy<>(new Provider<RandomAccessFile>() {
        @Override
        public RandomAccessFile provide() throws Exception {
            if (cache != null) {
                cache = File.createTempFile("_text_", ".tmp");
            }
            return new RandomAccessFile(cache, "rw");
        }
    });

    public FileCache() {
    }

    public FileCache(File cache) {
        this.cache = cache;
    }

    @Override
    public Object add(@NonNull String text) throws IOException {
        Validate.require(!closed, "closed");
        if (text.isEmpty()) {
            return RangeTag.EMPTY;
        }
        val writeLock = lock.writeLock();
        writeLock.lock();
        try {
            val raf = raf.get();
            Validate.checkNotNull(raf, "failed to create cache file");
            val tag = new RangeTag(raf.getFilePointer(), text.length() * 2);
            raf.write(text.getBytes(ENCODING));
            return tag;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String get(Object tag) throws IOException {
        Validate.require(!closed, "closed");
        if (tag instanceof RangeTag) {
            val rt = (RangeTag) tag;
            if (rt.length == 0) {
                return StringUtils.EMPTY_TEXT;
            }
            val readLock = lock.readLock();
            readLock.lock();
            try {
                val raf = raf.get();
                Validate.checkNotNull(raf, "failed to create cache file");
                raf.seek(rt.offset);
                byte[] b = new byte[(int) rt.length];
                raf.readFully(b);
                val str = new String(b, ENCODING);
                b = null;
                return str;
            } finally {
                readLock.unlock();
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (raf.isInitialized()) {
            val writeLock = lock.writeLock();
            writeLock.lock();
            try {
                raf.get().close();
                if (!cache.delete()) {
                    Log.e(TAG, "cannot delete cache file: %s", cache);
                }
                closed = true;
            } finally {
                writeLock.unlock();
            }
        }
    }
}
