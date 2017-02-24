package pw.phylame.commons.util;

import java.util.Random;

public final class RandomUtils {
    private RandomUtils() {
    }

    private static final Random random = new Random();

    public static int randInteger() {
        return random.nextInt();
    }

    public static int randInteger(int bottom, int top) {
        Validate.require(top >= bottom, "top(%s) must >= bottom(%s)", top, bottom);
        return random.nextInt(top - bottom) + bottom;
    }

    public static <T> T anyOf(T[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        return items[randInteger(0, items.length)];
    }
}
