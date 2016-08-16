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

package pw.phylame.ycl.io;

import lombok.NonNull;

import java.io.*;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * Utilities for file and I/O operations.
 */
public final class IOUtils {
    /**
     * File of builtin MIME mapping.
     */
    public static final String MIME_MAPPING_FILE = "mime.properties";

    /**
     * Mapping extension name to MIME type.
     */
    private static final Map<String, String> mimeMap = new HashMap<>();

    static {
        // load builtin mime mapping
        loadProperties(IOUtils.class.getResource(MIME_MAPPING_FILE), mimeMap);
    }

    /**
     * MIME type for any file.
     */
    public static final String UNKNOWN_MIME = "application/octet-stream";

    private IOUtils() {
    }

    /**
     * Default buffer size
     */
    public static int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Splits path components by separator of path and extension.
     *
     * @param path the path string
     * @return the indexes of last path and extension separators
     * @throws NullPointerException if the <code>path</code> is <code>null</code>
     */
    public static int[] splitPath(String path) {
        if (path == null) {
            throw new NullPointerException();
        }
        int sepPos, extPos = path.length();
        for (sepPos = extPos - 1; sepPos >= 0; --sepPos) {
            char ch = path.charAt(sepPos);
            if (ch == '.') {
                extPos = sepPos;
            } else if (ch == '/' || ch == '\\') {
                break;
            }
        }
        return new int[]{sepPos, extPos};
    }

    /**
     * Gets the full name of specified file.
     *
     * @param path the path string
     * @return the name string (baseName.extension)
     * @throws NullPointerException if the <code>path</code> is <code>null</code>
     */
    public static String getFullName(String path) {
        int index = splitPath(path)[0];
        return path.substring(index != 0 ? index + 1 : index);
    }

    /**
     * Gets the base name of specified file.
     *
     * @param path path of the file
     * @return the name string
     * @throws NullPointerException if the <code>path</code> is <code>null</code>
     */
    public static String getBaseName(String path) {
        int[] indexes = splitPath(path);
        return path.substring(indexes[0] + 1, indexes[1]);
    }

    /**
     * Gets the extension name of specified file.
     *
     * @param path path of file
     * @return string of extension. If not contain extension return empty string.
     * @throws NullPointerException if the <code>path</code> is <code>null</code>
     */
    public static String getExtension(String path) {
        int index = splitPath(path)[1];
        return index != path.length() ? path.substring(index + 1) : "";
    }


    /**
     * Maps specified <code>mime</code> with specified <code>extension</code> name
     *
     * @param mime      the mime string
     * @param extension the extension name
     * @since 2.4
     */
    public static void mapMimeType(String mime, String extension) {
        mimeMap.put(extension, mime);
    }

    /**
     * Returns the MIME type of specified file name.
     *
     * @param name path name of file
     * @return string of MIME or empty string if <code>name</code> is <code>null</code> or empty
     */
    public static String getMimeType(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        String extension = getExtension(name);
        if (extension.isEmpty()) {
            return UNKNOWN_MIME;
        }
        String mime = mimeMap.get(extension);
        return mime != null ? mime : UNKNOWN_MIME;
    }

    public static String readLine(@NonNull InputStream in) throws IOException {
        StringBuilder b = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1) {
            b.append(ch);
            if (ch == '\n') {
                break;
            }
        }
        return b.toString();
    }

    public interface ByteInput {
        int read(byte[] b, int off, int len) throws IOException;
    }

    public static ByteInput getByteInput(InputStream is) {
        if (!(is instanceof BufferedInputStream)) {
            is = new BufferedInputStream(is);
        }
        return new ISWrapper(is);
    }

    public static ByteInput getByteInput(RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

    public interface ByteOutput {
        void write(byte[] b, int off, int len) throws IOException;

        void flush() throws IOException;
    }

    public static ByteOutput getByteOutput(OutputStream os) {
        if (!(os instanceof BufferedOutputStream)) {
            os = new BufferedOutputStream(os);
        }
        return new OSWrapper(os);
    }

    public static ByteOutput getByteOutput(RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

    /**
     * Copies bytes from <code>ByteInput</code> to <code>ByteOutput</code>.
     *
     * @param input   input source
     * @param output  destination output
     * @param size    size of bytes to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(ByteInput input, ByteOutput output, int size, int bufSize) throws IOException {
        if (size == 0) {
            return 0;
        }
        byte[] bytes = new byte[bufSize];
        int n, total = 0;
        while ((n = input.read(bytes, 0, bufSize)) != -1) {
            total += n;
            if (size < 0 || total < size) {
                output.write(bytes, 0, n);
            } else {
                output.write(bytes, 0, n - (total - size));
                total = size;
                break;
            }
        }
        output.flush();
        return total;
    }

    public static int copy(ByteInput input, ByteOutput output, int size) throws IOException {
        return copy(input, output, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies characters from <code>Reader</code> to <code>Reader</code>.
     *
     * @param reader  source reader
     * @param writer  destination writer
     * @param size    size of characters to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied characters
     * @throws IOException if occur I/O error.
     */
    public static int copy(Reader reader, Writer writer, int size, int bufSize) throws IOException {
        if (size == 0) {
            return 0;
        }
        char[] chars = new char[bufSize];
        int n, total = 0;
        while ((n = reader.read(chars, 0, bufSize)) != -1) {
            total += n;
            if (size < 0 || total < size) {
                writer.write(chars, 0, n);
            } else {
                writer.write(chars, 0, n - (total - size));
                total = size;
                break;
            }
        }
        writer.flush();
        return total;
    }

    public static int copy(Reader reader, Writer writer, int size) throws IOException {
        return copy(reader, writer, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>InputStream</code> to <code>OutputStream</code>.
     *
     * @param in      source stream
     * @param out     destination stream
     * @param size    size of bytes to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(InputStream in, OutputStream out, int size, int bufSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufSize);
    }

    public static int copy(InputStream in, OutputStream out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>InputSteam</code> to <code>RandomAccessFile</code>.
     *
     * @param in      source stream
     * @param out     destination file
     * @param size    size of bytes to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(InputStream in, RandomAccessFile out, int size, int bufSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufSize);
    }

    public static int copy(InputStream in, RandomAccessFile out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>OutputStream</code>.
     *
     * @param in      source file
     * @param out     destination stream
     * @param size    size of bytes to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(RandomAccessFile in, OutputStream out, int size, int bufSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufSize);
    }

    public static int copy(RandomAccessFile in, OutputStream out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>RandomAccessFile</code>.
     *
     * @param in      source file
     * @param out     destination file
     * @param size    size of bytes to copy, <code>-1</code> to copy all
     * @param bufSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(RandomAccessFile in, RandomAccessFile out, int size, int bufSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufSize);
    }

    public static int copy(RandomAccessFile in, RandomAccessFile out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Close specified <code>Closeable</code> quietly.
     *
     * @param closeable the <code>Closeable</code> instance
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param in the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(ByteInput in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, getByteOutput(out), -1, DEFAULT_BUFFER_SIZE);
        return out.toByteArray();
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param in the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(InputStream in) throws IOException {
        return toBytes(getByteInput(in));
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param raf the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(RandomAccessFile raf) throws IOException {
        return toBytes(getByteInput(raf));
    }

    /**
     * Open a reader for specified input stream with specified encoding.
     *
     * @param in       the input stream
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the reader
     * @throws UnsupportedEncodingException if specified encoding is unsupported
     */
    public static Reader openReader(InputStream in, String encoding) throws UnsupportedEncodingException {
        return encoding != null ? new InputStreamReader(in, encoding) : new InputStreamReader(in);
    }

    /**
     * Opens a buffered reader for specified file with encoding
     *
     * @param file     the input file
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the reader
     * @throws IOException if occur I/O errors
     */
    public static BufferedReader openReader(File file, String encoding) throws IOException {
        return new BufferedReader(openReader(new FileInputStream(file), encoding));
    }

    /**
     * Open a writer for specified output stream with specified encoding.
     *
     * @param out      the out stream
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the writer
     * @throws UnsupportedEncodingException if specified encoding is unsupported
     */
    public static Writer openWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        return encoding != null ? new OutputStreamWriter(out, encoding) : new OutputStreamWriter(out);
    }

    /**
     * Opens a buffered writer for specified file with encoding
     *
     * @param file     the output file
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the writer
     * @throws IOException if occur I/O errors
     */
    public static BufferedWriter openWriter(File file, String encoding) throws IOException {
        return new BufferedWriter(openWriter(new FileOutputStream(file), encoding));
    }

    /**
     * Gets all characters of specified reader.
     *
     * @param reader the input reader
     * @return the string
     * @throws IOException if occur I/O error.
     */
    public static String toString(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        copy(reader, writer, -1, DEFAULT_BUFFER_SIZE);
        return writer.toString();
    }

    /**
     * Gets string from specified input with specified encoding.
     *
     * @param in       the input
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the string
     * @throws IOException if occur I/O error
     */
    public static String toString(ByteInput in, String encoding) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, getByteOutput(out), -1, DEFAULT_BUFFER_SIZE);
        return encoding != null ? out.toString(encoding) : out.toString();
    }

    /**
     * Gets string from specified input with specified encoding.
     *
     * @param in       the input
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the string
     * @throws IOException if occur I/O error
     */
    public static String toString(InputStream in, String encoding) throws IOException {
        return toString(getByteInput(in), encoding);
    }

    /**
     * Gets all lines of specified reader.
     *
     * @param reader    the input reader
     * @param skipEmpty <code>true</code> to skip empty lines
     * @return list of lines
     * @throws IOException if occur I/O error
     */
    public static List<String> toLines(Reader reader, boolean skipEmpty) throws IOException {
        BufferedReader br;
        if (reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty() || !skipEmpty) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Gets all lines of specified input stream.
     *
     * @param in        the input stream
     * @param encoding  encoding of the bytes or <code>null</code> to use default encoding
     * @param skipEmpty <code>true</code> to skip empty lines
     * @return list of lines
     * @throws IOException if occur I/O error
     */
    public static List<String> toLines(InputStream in, String encoding, boolean skipEmpty) throws IOException {
        return toLines(openReader(new BufferedInputStream(in), encoding), skipEmpty);
    }

    /**
     * Copies source file to target file.
     *
     * @param source  the source file
     * @param target  the target file
     * @param bufSize size of buffer area
     * @throws IOException if occur I/O errors
     */
    public static void copyFile(File source, File target, int bufSize) throws IOException {
        try (FileInputStream in = new FileInputStream(source); FileOutputStream out = new FileOutputStream(target)) {
            copy(in, out, -1, bufSize);
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        copyFile(source, target, 0x10000);  // 64k to be fastest
    }

    /**
     * Writes specified char sequence to file.
     *
     * @param file     the output file
     * @param cs       the char sequence
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @throws IOException if occur I/O errors
     */
    public static void write(File file, CharSequence cs, String encoding) throws IOException {
        try (BufferedWriter writer = openWriter(file, encoding)) {
            writer.write(cs.toString());
        }
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                ClassLoader classLoader = null;
                try {
                    classLoader = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                    // ignore
                }
                return classLoader;
            }
        });
    }

    public static Enumeration<URL> getResources(String name, ClassLoader loader) {
        return AccessController.doPrivileged(new FindResAction(name, loader));
    }

    /**
     * Loads mapped key, value pair from specified URL and store to map.
     *
     * @param url the input url
     * @param map map to store key and value
     * @return number of read key, value pair
     */
    public static int loadProperties(URL url, Map<String, String> map) {
        if (url == null) {
            return 0;
        }
        Properties prop = new Properties();
        try (InputStream in = url.openStream()) {
            prop.load(in);
        } catch (IOException e) {
            return 0;
        }
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return prop.size();
    }

    private static class ISWrapper implements ByteInput {
        private final InputStream stream;

        private ISWrapper(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }
    }

    private static class OSWrapper implements ByteOutput {
        private final OutputStream stream;

        private OSWrapper(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            stream.flush();
        }
    }

    private static class RAFWrapper implements ByteInput, ByteOutput {
        private final RandomAccessFile raf;

        private RAFWrapper(RandomAccessFile file) {
            this.raf = file;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return raf.read(b, off, len);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            raf.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {

        }
    }

    private static class FindResAction implements PrivilegedAction<Enumeration<URL>> {
        private final String name;
        private final ClassLoader loader;

        private FindResAction(String name, ClassLoader loader) {
            this.name = name;
            this.loader = loader;
        }

        @Override
        public Enumeration<URL> run() {
            Enumeration<URL> urls = null;
            try {
                urls = loader != null ? loader.getResources(name) : ClassLoader.getSystemResources(name);
            } catch (IOException | NoSuchMethodError e) {
                // ignored
            }
            return urls;
        }
    }
}
