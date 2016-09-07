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

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.util.CollectionUtils;
import pw.phylame.ycl.util.Function;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipArchive implements Archive<ZipItem> {
    private final ZipFile zip;

    public ZipArchive(@NonNull String path) throws IOException {
        this(new ZipFile(path));
    }

    public ZipArchive(@NonNull File file) throws IOException {
        this(new ZipFile(file));
    }

    public ZipArchive(@NonNull ZipFile zip) {
        this.zip = zip;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    @Override
    public String getName() {
        return zip.getName();
    }

    @Override
    public String getComment() {
        return zip.getComment();
    }

    @Override
    public ZipItem itemFor(@NonNull String name) {
        val e = zip.getEntry(name);
        return e != null ? new ZipItem(e) : null;
    }

    @Override
    public InputStream inputStreamOf(@NonNull ZipItem item) throws IOException {
        return zip.getInputStream(item.getEntry());
    }

    @Override
    public Iterable<? extends ZipItem> items() {
        return CollectionUtils.iterable(zip.entries(), new Function<Object, ZipItem>() {
            @Override
            public ZipItem apply(Object i) {
                return new ZipItem((ZipEntry) i);
            }
        });
    }

    @Override
    public int size() {
        return zip.size();
    }
}
