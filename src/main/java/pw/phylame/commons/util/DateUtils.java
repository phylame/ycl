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

package pw.phylame.commons.util;

import lombok.NonNull;
import lombok.val;
import pw.phylame.commons.function.Provider;
import pw.phylame.commons.log.Log;
import pw.phylame.commons.value.Lazy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private DateUtils() {
    }

    private static final String TAG = "DATEs";

    public static final String ISO_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    public static final String RFC1036_FORMAT = "EEEEEE, dd-MMM-yy HH:mm:ss z";

    public static final String ANSIC_FORMAT = "EEE MMM d HH:mm:ss z yyyy";

    private static final Lazy<DateFormat> isoFormatter = new Lazy<>(new Provider<DateFormat>() {
        @Override
        public DateFormat provide() throws Exception {
            return new SimpleDateFormat(ISO_FORMAT, Locale.ENGLISH);
        }
    });

    private static final Lazy<DateFormat> rfc1123Formatter = new Lazy<>(new Provider<DateFormat>() {
        @Override
        public DateFormat provide() {
            return new SimpleDateFormat(RFC1123_FORMAT, Locale.ENGLISH);
        }
    });

    private static final Lazy<DateFormat> rfc1036Formatter = new Lazy<>(new Provider<DateFormat>() {
        @Override
        public DateFormat provide() {
            return new SimpleDateFormat(RFC1036_FORMAT, Locale.ENGLISH);
        }
    });

    private static final Lazy<DateFormat> ansicFormatter = new Lazy<>(new Provider<DateFormat>() {
        @Override
        public DateFormat provide() {
            return new SimpleDateFormat(ANSIC_FORMAT, Locale.ENGLISH);
        }
    });

    public static String toISO(@NonNull Date date) {
        return isoFormatter.get().format(date);
    }

    public static Date forISO(@NonNull String str) throws ParseException {
        return isoFormatter.get().parse(str);
    }

    public static String toRFC1123(@NonNull Date date) {
        return rfc1123Formatter.get().format(date);
    }

    public static String toRFC822(@NonNull Date date) {
        return rfc1123Formatter.get().format(date);
    }

    public static Date forRFC1123(@NonNull String str) throws ParseException {
        return rfc1123Formatter.get().parse(str);
    }

    public static Date forRFC822(@NonNull String str) throws ParseException {
        return rfc1123Formatter.get().parse(str);
    }

    public static String toRFC1036(@NonNull Date date) {
        return rfc1036Formatter.get().format(date);
    }

    public static String toRFC850(@NonNull Date date) {
        return rfc1036Formatter.get().format(date);
    }

    public static Date forRFC1036(@NonNull String str) throws ParseException {
        return rfc1036Formatter.get().parse(str);
    }

    public static Date forRFC850(@NonNull String str) throws ParseException {
        return rfc1036Formatter.get().parse(str);
    }

    public static String toANSIC(@NonNull Date date) {
        return ansicFormatter.get().format(date);
    }

    public static Date forANSIC(@NonNull String str) throws ParseException {
        return ansicFormatter.get().parse(str);
    }

    public static Date parseDate(String str, Date defaultValue) {
        if (StringUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return forISO(str);
        } catch (ParseException e) {
            Log.e(TAG, e);
        }
        try {
            return forRFC1123(str);
        } catch (ParseException e) {
            Log.e(TAG, e);
        }
        try {
            return forRFC1036(str);
        } catch (ParseException e) {
            Log.e(TAG, e);
        }
        try {
            return forANSIC(str);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    public static String format(@NonNull Date date, @NonNull String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date parse(@NonNull String str, @NonNull String format) throws ParseException {
        return new SimpleDateFormat(format).parse(str);
    }

    public static Date parse(String str, @NonNull String format, Date fallback) {
        if (StringUtils.isEmpty(str)) {
            return fallback;
        }
        try {
            return new SimpleDateFormat(format).parse(str);
        } catch (ParseException e) {
            return fallback;
        }
    }

    public static Date calculate(@NonNull Date date, char unit, int amount) {
        final int field;
        switch (unit) {
            case 'y':
            case 'Y':
                field = Calendar.YEAR;
                break;
            case 'm':
            case 'M':
                field = Calendar.MONTH;
                break;
            case 'd':
            case 'D':
                field = Calendar.DAY_OF_MONTH;
                break;
            case 'h':
            case 'H':
                field = Calendar.HOUR_OF_DAY;
                break;
            case 'n':
            case 'N':
                field = Calendar.MINUTE;
                break;
            case 's':
            case 'S':
                field = Calendar.SECOND;
                break;
            default:
                throw Exceptions.forIllegalArgument("Invalid field type: %s, available: yYmMdDhHnNsS", unit);
        }
        val calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }
}
