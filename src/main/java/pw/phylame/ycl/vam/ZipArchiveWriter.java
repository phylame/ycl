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
import java.util.zip.ZipOutputStream;

import lombok.NonNull;
import pw.phylame.ycl.io.IOUtils;

public class ZipArchiveWriter implements ArchiveWriter<ZipItem> {
    private final ZipOutputStream zip;

    public ZipArchiveWriter(@NonNull String path) throws FileNotFoundException {
        this(new FileOutputStream(path));
    }

    public ZipArchiveWriter(@NonNull File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public ZipArchiveWriter(@NonNull OutputStream out) {
        this(new ZipOutputStream(out));
    }

    public ZipArchiveWriter(@NonNull ZipOutputStream zip) {
        this.zip = zip;
    }

    @Override
    public void setComment(@NonNull String comment) {
        zip.setComment(comment);
    }

    @Override
    public OutputStream begin(@NonNull ZipItem item) throws IOException {
        zip.putNextEntry(item.getEntry());
        return zip;
    }

    @Override
    public void end(@NonNull ZipItem item) throws IOException {
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull ZipItem item, @NonNull byte[] data, int off, int len) throws IOException {
        zip.putNextEntry(item.getEntry());
        zip.write(data, off, len);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull ZipItem item, @NonNull byte[] data) throws IOException {
        zip.putNextEntry(item.getEntry());
        zip.write(data, 0, data.length);
        zip.flush();
        zip.closeEntry();
    }

    @Override
    public void write(@NonNull ZipItem item, @NonNull InputStream input) throws IOException {
        zip.putNextEntry(item.getEntry());
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
