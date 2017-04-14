package pw.phylame.commons.cache;

import java.io.Closeable;
import java.io.IOException;

/**
 * A synchronized cache for string.
 */
public interface Cacheable extends Closeable {
    /**
     * Writes specified text to cache.
     *
     * @param text the text
     * @return tag for reading text from the cache
     */
    Object add(String text) throws IOException;

    /**
     * Reads text from the cache with specified tag.
     *
     * @param tag the tag for reading
     * @return the text, or {@literal null} if not found by the tag
     */
    String get(Object tag) throws IOException;
}
