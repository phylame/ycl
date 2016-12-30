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

import lombok.Getter;
import lombok.NonNull;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipItem implements VamItem {
    @Getter
    private final ZipEntry entry;

    ZipFile zip;

    public ZipItem(@NonNull String name) {
        this.entry = new ZipEntry(name);
    }

    public ZipItem(ZipEntry entry) {
        this.entry = entry;
    }

    @Override
    public String getName() {
        return entry.getName();
    }

    @Override
    public String getComment() {
        return entry.getComment();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public String toString() {
        return zip != null ? "zip://" + zip.getName() + '!' + entry.getName() : entry.getName();
    }

}
