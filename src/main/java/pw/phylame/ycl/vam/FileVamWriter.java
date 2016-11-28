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

package pw.phylame.ycl.vam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.io.IOUtils;
import pw.phylame.ycl.log.Log;
import pw.phylame.ycl.util.Exceptions;

public class FileVamWriter implements VamWriter<FileItem> {
    private static final String TAG = "FAW";

    private final File file;

    private final Map<String, OutputStream> streams = new IdentityHashMap<>();

    public FileVamWriter(@NonNull String path) throws IOException {
        this(new File(path));
    }

    public FileVamWriter(@NonNull File file) throws IOException {
        this.file = file;

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw Exceptions.forIO("Cannot create directory: %s", file);
            }
        } else if (file.isFile()) {
            throw Exceptions.forIllegalArgument("File must be directory: %s", file);
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void setComment(@NonNull String comment) {
        val item = new FileItem(FileItem.COMMENT_FILE);
        if (item.getFile().exists()) {
            return;
        }
        try {
            write(item, comment.getBytes());
        } catch (IOException e) {
            Log.e(TAG, e);
        }
    }

    private FileOutputStream openOutput(String name) throws FileNotFoundException {
        val file = new File(this.file, name);
        file.getParentFile().mkdirs();
        return new FileOutputStream(file);
    }

    private String itemKey(FileItem item) {
        return item.getFile().getPath();
    }

    @Override
    public OutputStream begin(@NonNull FileItem item) throws IOException {
        val name = itemKey(item);
        val out = openOutput(name);
        streams.put(name, out);
        return out;
    }

    @Override
    public void end(@NonNull FileItem item) throws IOException {
        val name = itemKey(item);
        val out = streams.get(name);
        if (out != null) {
            out.flush();
            out.close();
            streams.remove(name);
        }
    }

    @Override
    public void write(@NonNull FileItem item, @NonNull byte[] data, int off, int len) throws IOException {
        try (val out = openOutput(item.getFile().getPath())) {
            out.write(data, off, len);
        }
    }

    @Override
    public void write(@NonNull FileItem item, @NonNull byte[] data) throws IOException {
        write(item, data, 0, data.length);
    }

    @Override
    public void write(FileItem item, InputStream input) throws IOException {
        try (val out = openOutput(item.getFile().getPath())) {
            IOUtils.copy(input, out, -1);
        }
    }

    @Override
    public FileItem mkitem(@NonNull String name) {
        return new FileItem(name);
    }
}
