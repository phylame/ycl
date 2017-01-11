package pw.phylame.ycl.value;

import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.NonNull;
import lombok.SneakyThrows;
import pw.phylame.ycl.function.Consumer;
import pw.phylame.ycl.function.Provider;

public class Optional<T> implements Value<T> {
    private static final Optional<?> EMPTY = new Optional<>(null);

    private final T value;

    private Optional(T value) {
        this.value = value;
    }

    public static <T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    public static <T> Optional<T> of(@NonNull T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? Optional.<T>empty() : of(value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.consume(value);
    }

    @Override
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public T orElse(T fallback) {
        return value != null ? value : fallback;
    }

    @SneakyThrows(Exception.class)
    public T orElse(@NonNull Provider<? extends T> provider) {
        return value != null ? value : provider.provide();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Optional)) {
            return false;
        }
        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null ? String.format("Optional[%s]", value) : "Optional.empty";
    }
}
