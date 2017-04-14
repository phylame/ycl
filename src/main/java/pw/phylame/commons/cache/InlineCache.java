package pw.phylame.commons.cache;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pw.phylame.commons.util.StringUtils;

import java.io.IOException;

public class InlineCache implements Cacheable {
    private static final TextHolder EMPTY = new TextHolder(StringUtils.EMPTY_TEXT);

    @Override
    public Object add(@NonNull String text) {
        return text.isEmpty()
                ? EMPTY
                : new TextHolder(text);
    }

    @Override
    public String get(Object tag) {
        if (tag instanceof TextHolder) {
            return ((TextHolder) tag).text;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    @RequiredArgsConstructor
    private static class TextHolder {
        private final String text;
    }
}
