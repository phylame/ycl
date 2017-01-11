/*
 * Copyright 2017 Peng Wan <phylame@163.com>
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

package pw.phylame.ycl.io;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.val;
import pw.phylame.ycl.util.CollectionUtils;
import pw.phylame.ycl.util.Validate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Map;

import static pw.phylame.ycl.util.StringUtils.EMPTY_TEXT;
import static pw.phylame.ycl.util.StringUtils.isNotEmpty;

public final class HttpUtils {
    private HttpUtils() {
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static String makeQueryString(Object[] params, String encoding) {
        if (params.length == 0) {
            return EMPTY_TEXT;
        }
        Validate.require(params.length % 2 == 0, "length of strings must %2 = 0");
        val b = new StringBuilder(params.length * 8);
        val last = params.length - 2;
        for (int i = 0; i < params.length; i += 2) {
            b.append(params[i]).append('=').append(URLEncoder.encode(params[i + 1].toString(), encoding));
            if (i != last) {
                b.append('&');
            }
        }
        return b.toString();
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static String makeQueryString(Map<String, ?> params, String encoding) {
        if (CollectionUtils.isEmpty(params)) {
            return EMPTY_TEXT;
        }
        val b = new StringBuilder(params.size() * 8);
        int i = 0, last = params.size() - 1;
        for (val e : params.entrySet()) {
            b.append(e.getKey()).append('=').append(URLEncoder.encode(e.getValue().toString(), encoding));
            if (i++ != last) {
                b.append('&');
            }
        }
        return b.toString();
    }

    @Builder
    public static class Request {
        private String url;
        private String method;
        private String encoding;
        private Map<String, ?> parameters;
        private Map<String, ?> properties;
        private byte[] payload;

        private Boolean doInput;
        private Boolean doOutput;
        private Boolean useCaches;
        private int connectTimeout;
        private int readTimeout;

        public URLConnection connect() throws IOException {
            String url = this.url;
            Validate.check(isNotEmpty(url), "url cannot be null or empty");
            boolean isHttp = url.startsWith("http");
            if (isHttp && CollectionUtils.isNotEmpty(parameters)) {
                url += '?' + HttpUtils.makeQueryString(parameters, isNotEmpty(encoding) ? encoding : "UTF-8");
            }
            val conn = new URL(url).openConnection();
            if (isHttp) {
                performHttp((HttpURLConnection) conn);
            }
            conn.setDoInput(doInput != null ? doInput : true);
            conn.setDoOutput(doOutput != null ? doOutput : false);
            conn.setUseCaches(useCaches != null ? useCaches : true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            setProperties(conn);
            writeData(conn);
            conn.connect();
            return conn;
        }

        private void performHttp(HttpURLConnection conn) throws ProtocolException {
            conn.setRequestMethod(isNotEmpty(method) ? method.toUpperCase() : "GET");
        }

        private void setProperties(URLConnection conn) {
            if (CollectionUtils.isNotEmpty(properties)) {
                for (val e : properties.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue().toString());
                }
            }
        }

        private void writeData(URLConnection conn) throws IOException {
            if (doOutput == null || !doOutput) {
                return;
            }
            if (payload != null && payload.length != 0) {
                try (val out = conn.getOutputStream()) {
                    out.write(payload);
                    out.flush();
                }
            }
        }
    }
}
