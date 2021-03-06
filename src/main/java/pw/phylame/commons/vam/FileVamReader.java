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

package pw.phylame.commons.vam;

import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.function.Consumer;
import pw.phylame.commons.io.IOUtils;
import pw.phylame.commons.util.Exceptions;

import java.io.*;
import java.util.LinkedList;

public class FileVamReader implements VamReader {
    private final File file;

    public FileVamReader(@NonNull String path) throws FileNotFoundException {
        this.file = new File(path);
        init();
    }

    public FileVamReader(@NonNull File file) throws FileNotFoundException {
        this.file = file;
        init();
    }

    private void init() throws FileNotFoundException {
        if (!file.exists()) {
            throw Exceptions.forFileNotFound("Directory not exists: %s", file);
        }
        if (!file.isDirectory()) {
            throw Exceptions.forIllegalArgument("File must be a directory: %s", file);
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getComment() {
        val file = new File(this.file, FileItem.COMMENT_FILE);
        if (file.exists()) {
            try (val in = new FileInputStream(file)) {
                return IOUtils.toString(in, null);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public FileItem itemFor(@NonNull String name) {
        val item = new File(file, name);
        return item.exists() ? new FileItem(item, this) : null;
    }

    @Override
    public InputStream streamOf(@NonNull VamItem item) throws IOException {
        val fi = (FileItem) item;
        if (fi.getArchive() == null || fi.getArchive().get() != this) {
            return null;
        }
        return new FileInputStream(fi.getFile());
    }

    @Override
    public Iterable<? extends FileItem> items() {
        val items = new LinkedList<FileItem>();
        walkDir(file, new Consumer<File>() {
            @Override
            public void consume(File value) {
                if (!value.getName().equals(FileItem.COMMENT_FILE)) {
                    items.add(new FileItem(value, FileVamReader.this));
                }
            }
        });
        return items;
    }

    @Override
    public int size() {
        val counter = new ItemCounter();
        walkDir(file, counter);
        return counter.count;
    }

    private void walkDir(File dir, Consumer<File> consumer) {
        val items = dir.listFiles();
        if (items == null) {
            return;
        }
        for (val item : items) {
            if (item.isDirectory()) {
                walkDir(item, consumer);
            } else {
                consumer.consume(item);
            }
        }
    }

    private class ItemCounter implements Consumer<File> {
        private int count = 0;

        @Override
        public void consume(File value) {
            if (!value.getName().equals(FileItem.COMMENT_FILE)) {
                ++count;
            }
        }
    }

    @Override
    public String toString() {
        return "file://" + file.getPath();
    }

}
