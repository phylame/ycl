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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import pw.phylame.commons.function.Provider;
import pw.phylame.commons.log.Log;
import pw.phylame.commons.util.StringUtils;
import pw.phylame.commons.value.Lazy;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class TextCache implements Closeable {
    private static final String ENCODING = "UTF-16BE";
    public static final String TAG = TextCache.class.getSimpleName();

    private File cache;
    private Lazy<BufferedRandomAccessFile> raf = new Lazy<>(new Provider<BufferedRandomAccessFile>() {
        @Override
        public BufferedRandomAccessFile provide() throws Exception {
            if (cache == null) {
                cache = File.createTempFile("_text_cache_", ".tmp");
            }
            return new BufferedRandomAccessFile(cache, "rw");
        }
    });

    public TextCache() {
    }

    public TextCache(File cache) {
        this.cache = cache;
    }

    public Object add(@NonNull String text) {
        if (text.isEmpty()) {
            return new Tag(0, 0);
        }
        val raf = this.raf.get();
        Tag tag = null;
        try {
            tag = new Tag(raf.getFilePointer(), text.length() * 2);
            raf.write(text.getBytes(ENCODING));
        } catch (IOException e) {
            Log.e(TAG, e);
        }
        return tag;
    }

    public String get(Object tag) {
        if (tag instanceof Tag) {
            val id = (Tag) tag;
            if (id.length == 0) {
                return StringUtils.EMPTY_TEXT;
            } else {
                val raf = this.raf.get();
                try {
                    raf.seek(id.offset);
                    byte[] b = new byte[(int) id.length];
                    raf.readFully(b);
                    val str = new String(b, ENCODING);
                    b = null;
                    return str;
                } catch (IOException e) {
                    Log.e(TAG, e);
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (raf.isInitialized()) {
            try {
                raf.get().close();
            } finally {
                if (!cache.delete()) {
                    Log.e(TAG, "cannot delete file: %s", cache);
                }
            }
        }
    }

    @AllArgsConstructor
    private static class Tag {
        private long offset;
        private long length;
    }

    public static void main(String[] args) {
        try (val cache = new TextCache()) {
            System.out.println(cache.get(cache.add("Hello World 1")));
            System.out.println(cache.get(cache.add("Hello World 12")));
            System.out.println(cache.get(cache.add("Hello World 123")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
