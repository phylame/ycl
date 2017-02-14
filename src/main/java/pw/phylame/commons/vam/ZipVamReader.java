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
import pw.phylame.commons.function.Function;
import pw.phylame.commons.function.Functionals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pw.phylame.commons.util.CollectionUtils.iterable;
import static pw.phylame.commons.util.CollectionUtils.iterator;

public class ZipVamReader implements VamReader {
    private final ZipFile zip;

    public ZipVamReader(@NonNull String path) throws IOException {
        this(new ZipFile(path));
    }

    public ZipVamReader(@NonNull File file) throws IOException {
        this(new ZipFile(file));
    }

    public ZipVamReader(@NonNull ZipFile zip) {
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
        return e != null ? new ZipItem(e, zip) : null;
    }

    @Override
    public InputStream streamOf(@NonNull VamItem item) throws IOException {
        return zip.getInputStream(((ZipItem) item).getEntry());
    }

    @Override
    public Iterable<? extends ZipItem> items() {
        return iterable(Functionals.map(iterator(zip.entries()), new Function<ZipEntry, ZipItem>() {
            @Override
            public ZipItem apply(ZipEntry i) {
                return new ZipItem(i, zip);
            }
        }));
    }

    @Override
    public int size() {
        return zip.size();
    }

    @Override
    public String toString() {
        return "zip://" + zip.getName();
    }

}
