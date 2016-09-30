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
import java.lang.ref.WeakReference;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import pw.phylame.ycl.util.Exceptions;

@ToString
public class FileItem implements Item {
    static final String COMMENT_FILE = "__comment__";

    @Getter
    private final File file;

    @Getter(AccessLevel.PACKAGE)
    private final WeakReference<? extends FileArchive> archive;

    public FileItem(@NonNull String name) {
        this(new File(name), null);
        init();
    }

    public FileItem(@NonNull File file) {
        this(file, null);
        init();
    }

    FileItem(@NonNull File file, FileArchive archive) {
        this.file = file;
        this.archive = archive != null ? new WeakReference<>(archive) : null;
    }

    private void init() {
        if (file.isAbsolute()) {
            throw Exceptions.forIllegalArgument("Relative path is required: %s", file.getPath());
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }
}
