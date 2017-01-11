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

package pw.phylame.ycl.util;

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.function.Prediction;
import pw.phylame.ycl.log.Log;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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

    public static <T extends Hierarchical<T>> T locate(@NonNull T item, @NonNull Collection<Integer> indices) {
        for (val index : indices) {
            item = item.items().get(index < 0 ? item.size() + index : index);
        }
        return item;
    }

    public static <T extends Hierarchical<T>> T locate(@NonNull T item, @NonNull int[] indices) {
        for (val index : indices) {
            item = item.items().get(index < 0 ? item.size() + index : index);
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

    public static <T extends Hierarchical<T>> T find(@NonNull T item, @NonNull Prediction<? super T> filter) {
        return find(item, filter, 0, false);
    }

    public static <T extends Hierarchical<T>> T find(T item, Prediction<? super T> prediction, int from, boolean recursion) {
        Validate.requireNotNull(item);
        Validate.requireNotNull(prediction);
        val end = item.size();
        val items = item.items();
        T sub;
        for (int ix = from; ix < end; ++ix) {
            sub = items.get(ix);
            if (prediction.test(sub)) {
                return sub;
            }
            if (sub.size() > 0 && recursion) {
                sub = find(sub, prediction, 0, true);
                if (sub != null) {
                    return sub;
                }
            }
        }
        return null;
    }

    public static <T extends Hierarchical<T>> int select(T item, Prediction<? super T> prediction, List<? super T> result) {
        return select(item, prediction, result, -1, true);
    }

    public static <T extends Hierarchical<T>> int select(@NonNull T item,
                                                         @NonNull Prediction<? super T> prediction,
                                                         @NonNull List<? super T> result,
                                                         int limit,
                                                         boolean recursion) {
        if (limit <= 0) {
            return 0;
        }
        int count = 0;
        for (val sub : item) {
            if (count++ == limit) {
                break;
            } else if (prediction.test(sub)) {
                result.add(sub);
            }
            if (recursion && sub.size() > 0) {
                count += select(sub, prediction, result, limit, true);
            }
        }
        return count;
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                ClassLoader classLoader = null;
                try {
                    classLoader = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                    Log.e("Misc", ex);
                }
                return classLoader;
            }
        });
    }
}
