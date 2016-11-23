package pw.phylame.ycl.util;


import lombok.Getter;

import java.util.*;

public class TreeSupport<T extends Hierarchical<T>, P> {
    @Getter
    protected T current;

    @Getter
    protected final Set<T> selections = new LinkedHashSet<>();

    private final Queue<P> positions = new LinkedList<>();

    public int size() {
        return current.size();
    }

    public List<T> items() {
        return current.items();
    }

    public void setSelected(T item, boolean selected) {
        if (selected) {
            selections.add(item);
        } else {
            selections.remove(item);
        }
    }

    public final void enterItem(int index) {
        enterItem(current.items().get(index));
    }

    public void enterItem(T item) {
        current = item;
        selections.clear();
        positions.offer(currentPosition());
        onLevelChanged(null);
    }

    public void backTop() {
        current = current.getParent();
        selections.clear();
        onLevelChanged(positions.poll());
    }

    protected P currentPosition() {
        return null;
    }

    protected void onLevelChanged(P position) {

    }
}
