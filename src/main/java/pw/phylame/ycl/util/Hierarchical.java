package pw.phylame.ycl.util;

import java.util.List;

public interface Hierarchical<T extends Hierarchical<T>> extends Iterable<T> {
    int size();

    List<T> items();

    T getParent();
}
