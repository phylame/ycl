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

package pw.phylame.ycl.vam;

import lombok.NonNull;
import pw.phylame.ycl.io.IOUtils;

import java.io.*;
import java.util.zip.ZipOutputStream;

public class ZipVamWriter implements VamWriter {
    private final ZipOutputStream zip;

    public ZipVamWriter(@NonNull String path) throws FileNotFoundException {
        this(new FileOutputStream(path));
    }

    public ZipVamWriter(@NonNull File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public ZipVamWriter(@NonNull OutputStream out) {
        this(new ZipOutputStream(out));
    }

    public ZipVamWriter(@NonNull ZipOutputStream zip) {
        this.zip = zip;
    }

    @Override
    public void setComment(@NonNull String comment) {
        zip.setComment(comment);
    }

    @Override
    public OutputStream begin(@NonNull VamItem item) throws IOException {
        zip.putNextEntry(((ZipItem) item).getEntry());
        return zip;
    }

    @Override
    public void end(@NonNull VamItem item) throws IOException {
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull VamItem item, @NonNull byte[] data, int off, int len) throws IOException {
        zip.putNextEntry(((ZipItem) item).getEntry());
        zip.write(data, off, len);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull VamItem item, @NonNull byte[] data) throws IOException {
        zip.putNextEntry(((ZipItem) item).getEntry());
        zip.write(data, 0, data.length);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull VamItem item, @NonNull InputStream input) throws IOException {
        zip.putNextEntry(((ZipItem) item).getEntry());
        IOUtils.copy(input, zip, -1);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    @Override
    public ZipItem mkitem(@NonNull String name) {
        return new ZipItem(name);
    }
}
