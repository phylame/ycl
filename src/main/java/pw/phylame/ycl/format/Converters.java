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

package pw.phylame.ycl.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.util.DateUtils;
import pw.phylame.ycl.util.StringUtils;

public final class Converters {
    private Converters() {
    }

    private static final ConcurrentMap<Class<?>, Converter<?>> converters = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Converter<T> set(@NonNull Class<T> clazz, @NonNull Converter<? extends T> converter) {
        return (Converter<T>) converters.put(clazz, converter);
    }

    public static <T> boolean has(Class<T> clazz) {
        return converters.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> Converter<T> get(@NonNull Class<T> clazz) {
        return (Converter<T>) converters.get(clazz);
    }

    public static <T> String render(@NonNull T o, @NonNull Class<T> clazz) {
        return render(o, clazz, null);
    }

    public static <T> String render(@NonNull T o, @NonNull Class<T> clazz, String fallback) {
        val conv = get(clazz);
        return conv != null ? conv.render(o) : fallback;
    }

    public static <T> T parse(@NonNull String str, @NonNull Class<T> clazz) {
        return parse(str, clazz, null);
    }

    public static <T> T parse(@NonNull String str, @NonNull Class<T> clazz, T fallback) {
        val conv = get(clazz);
        return conv != null ? conv.parse(str) : fallback;
    }

    static {
        val sc = new AbstractConverter<String>() {
            @Override
            public String parse(String str) {
                return str;
            }
        };
        set(String.class, sc);
        set(CharSequence.class, sc);
        val bc = new AbstractConverter<Boolean>() {
            @Override
            public Boolean parse(String str) {
                return Boolean.parseBoolean(str);
            }
        };
        set(Boolean.class, bc);
        set(boolean.class, bc);
        val yc = new AbstractConverter<Byte>() {
            @Override
            public Byte parse(String str) {
                return Byte.parseByte(str);
            }
        };
        set(Byte.class, yc);
        set(byte.class, yc);
        val hc = new AbstractConverter<Short>() {
            @Override
            public Short parse(String str) {
                return Short.parseShort(str);
            }
        };
        set(Short.class, hc);
        set(short.class, hc);
        val ic = new AbstractConverter<Integer>() {
            @Override
            public Integer parse(String str) {
                return Integer.parseInt(str);
            }
        };
        set(Integer.class, ic);
        set(int.class, ic);
        val lc = new AbstractConverter<Long>() {
            @Override
            public Long parse(String str) {
                return Long.parseLong(str);
            }
        };
        set(Long.class, lc);
        set(long.class, lc);
        val fc = new AbstractConverter<Float>() {
            @Override
            public Float parse(String str) {
                return Float.parseFloat(str);
            }
        };
        set(Float.class, fc);
        set(float.class, fc);
        val dc = new AbstractConverter<Double>() {
            @Override
            public Double parse(String str) {
                return Double.parseDouble(str);
            }
        };
        set(Double.class, dc);
        set(double.class, dc);
        set(Date.class, new Converter<Date>() {
            @Override
            public Date parse(@NonNull String str) {
                return DateUtils.parseDate(str, null);
            }

            @Override
            public String render(@NonNull Date o) {
                return DateUtils.toISO(o);
            }
        });
        set(BigInteger.class, new AbstractConverter<BigInteger>() {
            @Override
            public BigInteger parse(@NonNull String str) {
                return new BigInteger(str);
            }
        });
        set(BigDecimal.class, new AbstractConverter<BigDecimal>() {
            @Override
            public BigDecimal parse(@NonNull String str) {
                return new BigDecimal(str);
            }
        });
        set(Locale.class, new Converter<Locale>() {
            @Override
            public Locale parse(@NonNull String str) {
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

            @Override
            public String render(@NonNull Locale o) {
                val country = o.getCountry();
                val language = o.getLanguage();
                return (StringUtils.isNotEmpty(country)) ? language + '-' + country : language;
            }
        });
    }
}
