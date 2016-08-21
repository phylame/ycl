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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import pw.phylame.ycl.util.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities for file and I/O operations.
 */
public final class IOUtils {
    private IOUtils() {
    }

    private static final String TAG = "IOs";

    public static final String CLASS_PATH_PREFIX = "!";

    /**
     * Default buffer size
     */
    public static int DEFAULT_BUFFER_SIZE = 8192;

    public interface ByteInput {
        int read(byte[] b, int off, int len) throws IOException;
    }

    public static ByteInput getByteInput(InputStream in) {
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        return new ISWrapper(in);
    }

    public static ByteInput getByteInput(RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

    public interface ByteOutput {
        void write(byte[] b, int off, int len) throws IOException;

        void flush() throws IOException;
    }

    public static ByteOutput getByteOutput(OutputStream out) {
        if (!(out instanceof BufferedOutputStream)) {
            out = new BufferedOutputStream(out);
        }
        return new OSWrapper(out);
    }

    public static ByteOutput getByteOutput(RandomAccessFile raf) {
        return new RAFWrapper(raf);
    }

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

    /**
     * Copies bytes from <code>ByteInput</code> to <code>ByteOutput</code>.
     *
     * @param input      input source
     * @param output     destination output
     * @param size       size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(ByteInput input, ByteOutput output, int size, int bufferSize) throws IOException {
        byte[] bytes = new byte[bufferSize];
        int n, total = 0;
        while ((n = input.read(bytes, 0, bufferSize)) != -1) {
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
     * @param reader     source reader
     * @param writer     destination writer
     * @param size       size of characters to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied characters
     * @throws IOException if occur I/O error.
     */
    public static int copy(Reader reader, Writer writer, int size, int bufferSize) throws IOException {
        char[] chars = new char[bufferSize];
        int n, total = 0;
        while ((n = reader.read(chars, 0, bufferSize)) != -1) {
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
     * @param in         source stream
     * @param out        destination stream
     * @param size       size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(InputStream in, OutputStream out, int size, int bufferSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufferSize);
    }

    public static int copy(InputStream in, OutputStream out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>InputSteam</code> to <code>RandomAccessFile</code>.
     *
     * @param in         source stream
     * @param out        destination file
     * @param size       size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(InputStream in, RandomAccessFile out, int size, int bufferSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufferSize);
    }

    public static int copy(InputStream in, RandomAccessFile out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>OutputStream</code>.
     *
     * @param in         source file
     * @param out        destination stream
     * @param size       size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(RandomAccessFile in, OutputStream out, int size, int bufferSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufferSize);
    }

    public static int copy(RandomAccessFile in, OutputStream out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies bytes from <code>RandomAccessFile</code> to <code>RandomAccessFile</code>.
     *
     * @param in         source file
     * @param out        destination file
     * @param size       size of bytes to copy, <code>-1</code> to copy all
     * @param bufferSize size of buffer area
     * @return number of copied bytes
     * @throws IOException if occur I/O error.
     */
    public static int copy(RandomAccessFile in, RandomAccessFile out, int size, int bufferSize) throws IOException {
        return copy(getByteInput(in), getByteOutput(out), size, bufferSize);
    }

    public static int copy(RandomAccessFile in, RandomAccessFile out, int size) throws IOException {
        return copy(in, out, size, DEFAULT_BUFFER_SIZE);
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
        StringWriter out = new StringWriter();
        copy(reader, out, -1, DEFAULT_BUFFER_SIZE);
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
     * @param source     the source file
     * @param target     the target file
     * @param bufferSize size of buffer area
     * @throws IOException if occur I/O errors
     */
    public static void copyFile(File source, File target, int bufferSize) throws IOException {
        try (FileInputStream in = new FileInputStream(source); FileOutputStream out = new FileOutputStream(target)) {
            copy(in, out, -1, bufferSize);
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

    public static URL resourceFor(@NonNull String path, ClassLoader loader) throws MalformedURLException {
        if (path.startsWith(CLASS_PATH_PREFIX)) {
            val name = path.substring(CLASS_PATH_PREFIX.length());
            return loader != null ? loader.getResource(name) : getContextClassLoader().getResource(name);
        } else if (path.matches("^[a-z]{2,}://.*")) {
            return new URL(path);
        } else {
            return new URL("file:///" + new File(path).getAbsolutePath());
        }
    }

    public static InputStream openResource(@NonNull String path, ClassLoader loader) throws IOException {
        return resourceFor(path, loader).openStream();
    }

    public static Enumeration<URL> resourcesFor(@NonNull String name, ClassLoader loader) {
        return AccessController.doPrivileged(new FindResourcesAction(name, loader));
    }

    @AllArgsConstructor
    private static class ISWrapper implements ByteInput {
        @NonNull
        private final InputStream stream;

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }
    }

    @AllArgsConstructor
    private static class OSWrapper implements ByteOutput {
        @NonNull
        private final OutputStream stream;

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            stream.flush();
        }
    }

    @AllArgsConstructor
    private static class RAFWrapper implements ByteInput, ByteOutput {
        @NonNull
        private final RandomAccessFile raf;

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

    @AllArgsConstructor
    private static class FindResourcesAction implements PrivilegedAction<Enumeration<URL>> {
        @NonNull
        private final String name;
        private final ClassLoader loader;

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
}
