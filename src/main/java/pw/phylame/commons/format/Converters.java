/*
 * Copyright 2017 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.commons.format;

import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.util.DateUtils;
import pw.phylame.commons.util.MiscUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Converters {
    private Converters() {
    }

    private static final Map<Class<?>, Converter<?>> converters = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Converter<T> register(@NonNull Class<T> type, @NonNull Converter<? extends T> converter) {
        return (Converter<T>) converters.put(type, converter);
    }

    public static <T> boolean isRegistered(Class<T> type) {
        return converters.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Converter<T> forType(Class<T> type) {
        return (Converter<T>) converters.get(type);
    }

    public static <T> String render(@NonNull T o, @NonNull Class<T> type) {
        return render(o, type, null);
    }

    public static <T> String render(@NonNull T o, @NonNull Class<T> type, String fallback) {
        val conv = forType(type);
        return conv != null ? conv.render(o) : fallback;
    }

    public static <T> T parse(@NonNull String str, @NonNull Class<T> clazz) {
        return parse(str, clazz, null);
    }

    public static <T> T parse(@NonNull String str, @NonNull Class<T> clazz, T fallback) {
        val conv = forType(clazz);
        return conv != null ? conv.parse(str) : fallback;
    }

    static {
        val sc = new AbstractConverter<String>() {
            @Override
            public String parse(String str) {
                return str;
            }
        };
        register(String.class, sc);
        register(CharSequence.class, sc);
        val bc = new AbstractConverter<Boolean>() {
            @Override
            public Boolean parse(String str) {
                return Boolean.parseBoolean(str);
            }
        };
        register(Boolean.class, bc);
        register(boolean.class, bc);
        val yc = new AbstractConverter<Byte>() {
            @Override
            public Byte parse(String str) {
                return Byte.parseByte(str);
            }
        };
        register(Byte.class, yc);
        register(byte.class, yc);
        val hc = new AbstractConverter<Short>() {
            @Override
            public Short parse(String str) {
                return Short.parseShort(str);
            }
        };
        register(Short.class, hc);
        register(short.class, hc);
        val ic = new AbstractConverter<Integer>() {
            @Override
            public Integer parse(String str) {
                return Integer.parseInt(str);
            }
        };
        register(Integer.class, ic);
        register(int.class, ic);
        val lc = new AbstractConverter<Long>() {
            @Override
            public Long parse(String str) {
                return Long.parseLong(str);
            }
        };
        register(Long.class, lc);
        register(long.class, lc);
        val fc = new AbstractConverter<Float>() {
            @Override
            public Float parse(String str) {
                return Float.parseFloat(str);
            }
        };
        register(Float.class, fc);
        register(float.class, fc);
        val dc = new AbstractConverter<Double>() {
            @Override
            public Double parse(String str) {
                return Double.parseDouble(str);
            }
        };
        register(Double.class, dc);
        register(double.class, dc);
        register(Date.class, new Converter<Date>() {
            @Override
            public Date parse(@NonNull String str) {
                return DateUtils.parseDate(str, null);
            }

            @Override
            public String render(@NonNull Date o) {
                return DateUtils.toISO(o);
            }
        });
        register(BigInteger.class, new AbstractConverter<BigInteger>() {
            @Override
            public BigInteger parse(@NonNull String str) {
                return new BigInteger(str);
            }
        });
        register(BigDecimal.class, new AbstractConverter<BigDecimal>() {
            @Override
            public BigDecimal parse(@NonNull String str) {
                return new BigDecimal(str);
            }
        });
        register(Number.class, new AbstractConverter<Number>() {
            @Override
            public Number parse(String str) {
                try {
                    return NumberFormat.getInstance().parse(str);
                } catch (ParseException e) {
                    throw new NumberFormatException(e.getMessage());
                }
            }
        });
        register(Locale.class, new Converter<Locale>() {
            @Override
            public Locale parse(@NonNull String str) {
                return MiscUtils.parseLocale(str);
            }

            @Override
            public String render(@NonNull Locale o) {
                return MiscUtils.renderLocale(o);
            }
        });
    }
}
