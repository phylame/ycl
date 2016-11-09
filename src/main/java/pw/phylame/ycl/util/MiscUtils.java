package pw.phylame.ycl.util;

import lombok.NonNull;
import lombok.val;

import java.util.List;

public final class MiscUtils {
    private MiscUtils() {
    }

    private static <T extends Hierarchical<T>> T itemAt(T item, int index) {
        return item.items().get(index);
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
}
