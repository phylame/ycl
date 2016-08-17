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

package pw.phylame.ycl.io;

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.util.Provider;
import pw.phylame.ycl.util.StringUtils;
import pw.phylame.ycl.value.Lazy;
import pw.phylame.ycl.value.Pair;

import java.util.Properties;

/**
 * Utilities for file name operations.
 */
public final class PathUtils {

    private PathUtils() {
    }

    /**
     * MIME type for any file.
     */
    public static final String UNKNOWN_MIME = "application/octet-stream";

    public static Pair<Integer, Integer> split(@NonNull String path) {
        int extpos = path.length(), seppos;
        for (seppos = extpos - 1; seppos >= 0; --seppos) {
            char ch = path.charAt(seppos);
            if (ch == '.') {
                extpos = seppos;
            } else if (ch == '/' || ch == '\\') {
                break;
            }
        }
        return new Pair<>(seppos, extpos);
    }

    public static String fullName(@NonNull String path) {
        int seppos = split(path).getFirst();
        return path.substring(seppos != 0 ? seppos + 1 : seppos);
    }

    public static String baseName(@NonNull String path) {
        val pair = split(path);
        return path.substring(pair.getFirst() + 1, pair.getSecond());
    }

    public static String extensionName(@NonNull String path) {
        int extsep = split(path).getSecond();
        return extsep != path.length() ? path.substring(extsep + 1) : "";
    }

    private static final String MIME_MAPPING_FILE = "/pw/phylame/ycl/io/mime.properties";

    private static final Lazy<Properties> mimeMap = new Lazy<>(new Provider<Properties>() {
        @Override
        public Properties provide() throws Exception {
            val prop = new Properties();
            val in = PathUtils.class.getResourceAsStream(MIME_MAPPING_FILE);
            if (in != null) {
                prop.load(in);
            }
            return prop;
        }
    }, new Properties());

    public static void mapMime(@NonNull String extension, @NonNull String mime) {
        mimeMap.get().setProperty(extension, mime);
    }

    public static String mimeFor(@NonNull String name) {
        if (name.isEmpty()) {
            return "";
        }
        val ext = extensionName(name);
        if (ext.isEmpty()) {
            return UNKNOWN_MIME;
        }
        return mimeMap.get().getProperty(name, UNKNOWN_MIME);
    }

    public static String mimeOrDetect(@NonNull String path, String mime) {
        return StringUtils.isEmpty(mime) ? mimeFor(path) : mime;
    }
}