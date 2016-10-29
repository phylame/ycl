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
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import pw.phylame.ycl.log.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * Utilities for file and I/O operations.
 */
public final class IOUtils {
    private IOUtils() {
    }

    private static final String TAG = "IOs";

    /**
     * Prefix for path name in class path.
     */
    public static final String CLASS_PATH_PREFIX = "!";

    /**
     * Default buffer size.
     */
    public static int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * End of line.
     */
    public static final int EOF = -1;

    /**
     * Close specified <code>Closeable</code> quietly.
     *
     * @param c the <code>Closeable</code> instance
     */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, e);
            }
        }
    }

    public static BufferedInputStream buffered(@NonNull InputStream in) {
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
    }

    public static BufferedOutputStream buffered(@NonNull OutputStream out) {
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream((out));
    }

    public static BufferedReader buffered(@NonNull Reader reader) {
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedWriter buffered(@NonNull Writer writer) {
        return (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    public interface Reading {
        int read(byte[] b, int off, int len) throws IOException;
    }

    public interface Writing {
        void write(byte[] b, int off, int len) throws IOException;

        void flush() throws IOException;
    }

    public static Reading readingFor(@NonNull InputStream in) {
        return new ISWrapper(in);
    }

    public static Reading readingFor(@NonNull RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

    public static Writing writingFor(@NonNull OutputStream out) {
        return new OSWrapper(out);
    }

    public static Writing writingFor(@NonNull RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

    public static long copy(InputStream in, OutputStream out, int limit) throws IOException {
        return copy(in, out, limit, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>InputStream</code> to <code>OutputStream</code>.
     *
     * @param in         source stream
     * @param out        destination stream
     * @param limit      size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static long copy(InputStream in, OutputStream out, int limit, int bufferSize) throws IOException {
        return copy(readingFor(in), writingFor(out), limit, bufferSize);
    }

    public static long copy(InputStream in, RandomAccessFile out, int limit) throws IOException {
        return copy(in, out, limit, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>InputSteam</code> to <code>RandomAccessFile</code>.
     *
     * @param in         source stream
     * @param out        destination file
     * @param limit      size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static long copy(InputStream in, RandomAccessFile out, int limit, int bufferSize) throws IOException {
        return copy(readingFor(in), writingFor(out), limit, bufferSize);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>OutputStream</code>.
     *
     * @param in         source file
     * @param out        destination stream
     * @param limit      size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static long copy(RandomAccessFile in, OutputStream out, int limit, int bufferSize) throws IOException {
        return copy(readingFor(in), writingFor(out), limit, bufferSize);
    }

    public static long copy(RandomAccessFile in, OutputStream out, int limit) throws IOException {
        return copy(in, out, limit, DEFAULT_BUFFER_SIZE);
    }

    public static long copy(RandomAccessFile in, RandomAccessFile out, int limit) throws IOException {
        return copy(in, out, limit, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>RandomAccessFile</code>.
     *
     * @param in         source file
     * @param out        destination file
     * @param limit      size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static long copy(RandomAccessFile in, RandomAccessFile out, int limit, int bufferSize) throws IOException {
        return copy(readingFor(in), writingFor(out), limit, bufferSize);
    }

    public static long copy(Reading input, Writing output, int limit) throws IOException {
        return copy(input, output, limit, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>Reading</code> to <code>Writing</code>.
     *
     * @param in         input source
     * @param out        destination output
     * @param limit      size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static long copy(@NonNull Reading in, @NonNull Writing out, long limit, int bufferSize) throws IOException {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        val bytes = new byte[bufferSize];
        int n;
        long total = 0L;
        while ((n = in.read(bytes, 0, bufferSize)) != EOF) {
            total += n;
            if (limit < 0 || total < limit) {
                out.write(bytes, 0, n);
            } else {
                out.write(bytes, 0, n - (int) (total - limit));
                total = limit;
                break;
            }
        }
        out.flush();
        return total;
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param in the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(InputStream in) throws IOException {
        return toBytes(readingFor(buffered(in)));
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param raf the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(RandomAccessFile raf) throws IOException {
        return toBytes(readingFor(raf));
    }

    /**
     * Gets all bytes of specified input.
     *
     * @param in the input
     * @return the bytes
     * @throws IOException if occur I/O error
     */
    public static byte[] toBytes(Reading in) throws IOException {
        val out = new ByteArrayOutputStream();
        copy(in, writingFor(out), -1, DEFAULT_BUFFER_SIZE);
        return out.toByteArray();
    }

    public static Reader readerFor(File file) throws IOException {
        return readerFor(file, null);
    }

    /**
     * Opens a reader for specified file with encoding.
     *
     * @param file     the input file
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the reader
     * @throws IOException if occur I/O errors
     */
    public static Reader readerFor(File file, String encoding) throws IOException {
        return readerFor(new FileInputStream(file), encoding);
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static Reader readerFor(InputStream in) {
        return readerFor(in, null);
    }

    /**
     * Open a reader for specified input stream with specified encoding.
     *
     * @param in       the input stream
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the reader
     * @throws UnsupportedEncodingException if specified encoding is unsupported
     */
    public static Reader readerFor(InputStream in, String encoding) throws UnsupportedEncodingException {
        return encoding != null ? new InputStreamReader(in, encoding) : new InputStreamReader(in);
    }

    public static Writer writerFor(File file) throws IOException {
        return writerFor(file, null);
    }

    /**
     * Opens a writer for specified file with encoding.
     *
     * @param file     the output file
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the writer
     * @throws IOException if occur I/O errors
     */
    public static Writer writerFor(File file, String encoding) throws IOException {
        return writerFor(new FileOutputStream(file), encoding);
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static Writer writerFor(OutputStream out) {
        return writerFor(out, null);
    }

    /**
     * Open a writer for specified output stream with specified encoding.
     *
     * @param out      the out stream
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the writer
     * @throws UnsupportedEncodingException if specified encoding is unsupported
     */
    public static Writer writerFor(OutputStream out, String encoding) throws UnsupportedEncodingException {
        return encoding != null ? new OutputStreamWriter(out, encoding) : new OutputStreamWriter(out);
    }

    public static long copy(Reader reader, Writer writer, int limit) throws IOException {
        return copy(reader, writer, limit, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies characters from <code>Reader</code> to <code>Reader</code>.
     *
     * @param in         source reader
     * @param out        destination writer
     * @param limit      size of characters to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied characters
     * @throws IOException if occur I/O error.
     */
    public static long copy(@NonNull Reader in, @NonNull Writer out, long limit, int bufferSize) throws IOException {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        val chars = new char[bufferSize];
        int n;
        long total = 0L;
        while ((n = in.read(chars, 0, bufferSize)) != EOF) {
            total += n;
            if (limit < 0 || total < limit) {
                out.write(chars, 0, n);
            } else {
                out.write(chars, 0, n - (int) (total - limit));
                total = limit;
                break;
            }
        }
        out.flush();
        return total;
    }

    /**
     * Gets all characters of specified reader.
     *
     * @param reader the input reader
     * @return the string
     * @throws IOException if occur I/O error.
     */
    public static String toString(Reader reader) throws IOException {
        val out = new StringWriter();
        copy(buffered(reader), out, -1, DEFAULT_BUFFER_SIZE);
        return out.toString();
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
        return toString(readingFor(buffered(in)), encoding);
    }

    /**
     * Gets string from specified input with specified encoding.
     *
     * @param in       the input
     * @param encoding the encoding, if <code>null</code> use default encoding
     * @return the string
     * @throws IOException if occur I/O error
     */
    public static String toString(Reading in, String encoding) throws IOException {
        val out = new ByteArrayOutputStream();
        copy(in, writingFor(out), -1, DEFAULT_BUFFER_SIZE);
        return encoding != null ? out.toString(encoding) : out.toString();
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
        return toLines(readerFor(buffered(in), encoding), skipEmpty);
    }

    /**
     * Gets all lines of specified reader.
     *
     * @param reader    the input reader
     * @param skipEmpty <code>true</code> to skip empty lines
     * @return list of lines
     * @throws IOException if occur I/O error
     */
    public static List<String> toLines(@NonNull Reader reader, boolean skipEmpty) throws IOException {
        val br = buffered(reader);
        val lines = new LinkedList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty() || !skipEmpty) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static Iterator<String> linesOf(InputStream in, String encoding, boolean skipEmpty) throws IOException {
        return linesOf(readerFor(buffered(in), encoding), skipEmpty);
    }

    public static Iterator<String> linesOf(@NonNull Reader reader, boolean skipEmpty) {
        return new LineIterator(skipEmpty, buffered(reader));
    }

    public static void copyFile(File source, File target) throws IOException {
        copyFile(source, target, 0x10000); // 64k to be fastest
    }

    /**
     * Copies source file to target file.
     *
     * @param source     the source file
     * @param target     the target file
     * @param bufferSize size of buffer area
     * @throws IOException if occur I/O errors
     */
    public static void copyFile(File source, File target, int bufferSize) throws IOException {
        try (val in = new FileInputStream(source); FileOutputStream out = new FileOutputStream(target)) {
            copy(in, out, -1, bufferSize);
        }
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
        try (val writer = writerFor(file, encoding)) {
            writer.write(cs.toString());
        }
    }

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                ClassLoader classLoader = null;
                try {
                    classLoader = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                    Log.e(TAG, ex);
                }
                return classLoader;
            }
        });
    }

    public static URL resourceFor(@NonNull String path) throws MalformedURLException {
        return resourceFor(path, null);
    }

    public static URL resourceFor(@NonNull String path, ClassLoader loader) throws MalformedURLException {
        if (path.startsWith(CLASS_PATH_PREFIX)) {
            val name = path.substring(CLASS_PATH_PREFIX.length());
            return loader != null ? loader.getResource(name) : getContextClassLoader().getResource(name);
        } else if (path.matches("^[a-z]{2,}://.*")) {
            return new URL(path);
        } else {
            val file = new File(path);
            return file.exists() ? new URL("file:///" + file.getAbsolutePath()) : null;
        }
    }

    public static Enumeration<URL> resourcesFor(@NonNull String name) {
        return resourcesFor(name, null);
    }

    public static Enumeration<URL> resourcesFor(@NonNull String name, ClassLoader loader) {
        return AccessController.doPrivileged(new FindResourcesAction(name, loader));
    }

    public static InputStream openResource(@NonNull String path) throws IOException {
        return openResource(path, null);
    }

    public static InputStream openResource(@NonNull String path, ClassLoader loader) throws IOException {
        val url = resourceFor(path, loader);
        return url != null ? url.openStream() : null;
    }

    @Value
    private static class ISWrapper implements Reading {
        @NonNull
        private InputStream stream;

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }
    }

    @Value
    private static class OSWrapper implements Writing {
        @NonNull
        private OutputStream stream;

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            stream.flush();
        }
    }

    @Value
    private static class RAFWrapper implements Reading, Writing {
        @NonNull
        private RandomAccessFile raf;

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

    @Value
    private static class FindResourcesAction implements PrivilegedAction<Enumeration<URL>> {
        @NonNull
        private String name;
        private ClassLoader loader;

        @Override
        public Enumeration<URL> run() {
            Enumeration<URL> urls = null;
            try {
                urls = loader != null ? loader.getResources(name) : ClassLoader.getSystemResources(name);
            } catch (IOException | NoSuchMethodError e) {
                Log.e(TAG, e);
            }
            return urls;
        }
    }

    private static class LineIterator implements Iterator<String> {
        private final boolean skipEmpty;

        private final BufferedReader reader;

        private String nextLine = null;
        private boolean done = false;

        private LineIterator(boolean skipEmpty, BufferedReader reader) {
            this.skipEmpty = skipEmpty;
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            if (nextLine == null && !done) {
                try {
                    nextLine = reader.readLine();
                } catch (IOException e) {
                    nextLine = null;
                }
                if (nextLine == null) {
                    done = true;
                } else if (nextLine.isEmpty() && skipEmpty) {
                    done = true;
                }
            }
            return nextLine != null;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            val answer = nextLine;
            nextLine = null;
            return answer;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
