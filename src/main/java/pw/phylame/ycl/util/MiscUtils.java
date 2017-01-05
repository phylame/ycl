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

package pw.phylame.ycl.util;

import java.util.List;
import java.util.Locale;

import lombok.NonNull;
import lombok.val;

public final class MiscUtils {
    private MiscUtils() {
    }

    public static String renderLocale(@NonNull Locale locale) {
        val country = locale.getCountry();
        val language = locale.getLanguage();
        return (StringUtils.isNotEmpty(country)) ? language + '-' + country : language;
    }

    public static Locale parseLocale(@NonNull String str) {
        int index = str.indexOf('-');
        if (index == -1) {
            index = str.indexOf('_');
        }
        String language;
        String country;
        if (index == -1) {
            language = str;
            country = "";
        } else {
            language = str.substring(0, index);
            country = str.substring(index + 1);
        }
        return new Locale(language, country);
    }

    public static <T extends Hierarchical<T>> T locate(@NonNull T item, @NonNull int[] indices) {
        for (val index : indices) {
            item = itemAt(item, index < 0 ? item.size() + index : index);
        }
        return item;
    }

    public static <T extends Hierarchical<T>> int depthOf(@NonNull T item) {
        if (item.size() == 0) {
            return 0;
        }

        int depth = 0;
        for (val sub : item) {
            int d = depthOf(sub);
            if (d > depth) {
                depth = d;
            }
        }

        return depth + 1;
    }

    public static <T extends Hierarchical<T>> T find(@NonNull T item, @NonNull Function<T, Boolean> filter) {
        return find(item, filter, 0, false);
    }

    public static <T extends Hierarchical<T>> T find(@NonNull T item, @NonNull Function<T, Boolean> filter, int from,
            boolean recursion) {
        val end = item.size();
        val items = item.items();

        T sub;
        for (int ix = from; ix < end; ++ix) {
            sub = items.get(ix);
            if (filter.apply(sub)) {
                return sub;
            }
            if (sub.size() > 0 && recursion) {
                sub = find(sub, filter, 0, true);
                if (sub != null) {
                    return sub;
                }
            }
        }

        return null;
    }

    public static <T extends Hierarchical<T>> int select(@NonNull T item, @NonNull Function<T, Boolean> filter,
            @NonNull List<T> result) {
        return select(item, filter, result, -1, true);
    }

    public static <T extends Hierarchical<T>> int select(@NonNull T item,
            @NonNull Function<T, Boolean> filter,
            @NonNull List<T> result,
            int limit,
            boolean recursion) {
        if (limit <= 0) {
            return 0;
        }

        int count = 0;
        for (val sub : item) {
            if (count++ == limit) {
                break;
            } else if (filter.apply(sub)) {
                result.add(sub);
            }
            if (recursion && sub.size() > 0) {
                count += select(sub, filter, result, limit, true);
            }
        }

        return count;
    }

    private static <T extends Hierarchical<T>> T itemAt(T item, int index) {
        return item.items().get(index);
    }
}
