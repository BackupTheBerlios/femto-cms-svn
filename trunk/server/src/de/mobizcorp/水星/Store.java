/*
 * 水星 - Water Star.
 * Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.mobizcorp.水星;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.mobizcorp.水星.Lock.LockFailed;

/**
 * Local SCM store.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Store {

    public static class TagEntry implements Comparable<TagEntry> {
        private static Collator c;

        private static synchronized Collator getCollator() {
            if (c == null) {
                c = Collator.getInstance();
            }
            return c;
        }

        private transient CollationKey ck;

        public final int g;

        public final String t;

        public final Version v;

        public TagEntry(final int g, final String t, final Version v) {
            this.g = g;
            this.t = t;
            this.v = v;
        }

        public int compareTo(TagEntry other) {
            return this.g == other.g ? this.getCollationKey().compareTo(
                    other.getCollationKey()) : this.g < other.g ? -1 : 1;
        }

        private synchronized CollationKey getCollationKey() {
            if (ck == null) {
                ck = getCollator().getCollationKey(t);
            }
            return ck;
        }
    }

    private static final String HG_TAGS = ".hgtags";

    private static final Deflater sharedDeflater = new Deflater();

    private static final Inflater sharedInflater = new Inflater();

    private static final String TIP = "tip";

    private static final byte[] EMPTY = new byte[0];

    private static void addTag(HashMap<String, Version> tags, String text) {
        int mark = text.lastIndexOf(' ');
        if (mark != -1) {
            String n = trim(text, 0, mark);
            String k = text.substring(mark + 1);
            tags.put(k, Version.create(n));
        }
    }

    private static void addTags(HashMap<String, Version> tags,
            final BufferedReader file) throws IOException {
        String line;
        while ((line = file.readLine()) != null) {
            addTag(tags, line);
        }
    }

    public static byte[] compress(byte[] data) {
        return compress(data, 0, data.length);
    }

    public static byte[] compress(final byte[] data, final int off,
            final int len) {
        if (len < 44) {
            if (len > 0 && data[0] != 0) {
                final byte[] grow = new byte[len + 1];
                grow[0] = 'u';
                System.arraycopy(data, off, grow, 1, len);
                return grow;
            } else {
                return data;
            }
        }
        final byte[] buffer = new byte[len + 1];
        synchronized (Store.class) {
            final Deflater d = sharedDeflater;
            d.reset();
            d.setInput(data, off, len);
            d.finish();
            final int n = d.deflate(buffer);
            if (d.finished() && n < buffer.length) {
                final byte[] shrink = new byte[n];
                System.arraycopy(buffer, 0, shrink, 0, n);
                return shrink;
            }
        }
        if (len > 0 && data[0] != 0) {
            buffer[0] = 'u';
            System.arraycopy(data, off, buffer, 1, len);
            return buffer;
        } else {
            return data;
        }
    }

    public static byte[] decompress(final byte[] data)
            throws DataFormatException {
        return decompress(data, 0, data.length);
    }

    public static byte[] decompress(final byte[] data, final int off,
            final int len) throws DataFormatException {
        if (len > 0) {
            int b = data[0];
            if (b == 0x78) {
                final ArrayList<byte[]> chain = new ArrayList<byte[]>();
                byte[] buffer = new byte[len * 2];
                int fill = 0;
                int size = 0;
                synchronized (Store.class) {
                    Inflater i = sharedInflater;
                    i.reset();
                    i.setInput(data, off, len);
                    while (!i.finished()) {
                        if (i.needsInput()) {
                            throw new DataFormatException(
                                    "compressed data truncated");
                        } else {
                            int n = i.inflate(buffer, fill, buffer.length
                                    - fill);
                            size += n;
                            if ((fill += n) == buffer.length) {
                                chain.add(buffer);
                                buffer = new byte[buffer.length];
                                fill = 0;
                            }
                        }
                    }
                }
                final byte[] grow = new byte[size];
                fill = 0;
                for (byte[] chunk : chain) {
                    int n = chunk.length;
                    System.arraycopy(chunk, 0, grow, fill, n);
                    fill += n;
                }
                if (fill < size) {
                    System.arraycopy(buffer, 0, grow, fill, size - fill);
                }
                return grow;
            } else if (b == 'u') {
                final byte[] shrink = new byte[len - 1];
                System.arraycopy(data, off + 1, shrink, 0, len - 1);
                return shrink;
            } else if (b != 0) {
                throw new DataFormatException("unknown compression type" + b);
            }
        }
        return data;
    }

    public static File findBase(final File cwd) {
        File scan = cwd;
        try {
            while (scan != null) {
                File store = new File(scan, ".hg");
                if (store.isDirectory()) {
                    return scan;
                } else {
                    File next = scan.getParentFile();
                    if (next == null) {
                        next = scan.getAbsoluteFile().getParentFile();
                    }
                    if (next.equals(scan)) {
                        // in case getParentFile returns the same
                        break;
                    } else {
                        scan = next;
                    }
                }
            }
        } catch (NullPointerException e) {
            // in case getParentFile returns null
        }
        throw new IllegalArgumentException("no store found for '" + cwd + "'");
    }

    public static File findBase(String cwd) {
        return findBase(new File(cwd));
    }

    private static void forceRename(File from, File to) {
        if (!from.renameTo(to)) {
            to.delete();
            from.renameTo(to);
        }
    }

    public static boolean startsWith(final byte[] data, final byte[] match) {
        int scan = match.length;
        if (scan > data.length) {
            return false;
        }
        while (--scan >= 0) {
            if (data[scan] != match[scan]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] toBytes(String str) {
        try {
            return (str == null || str.length() == 0) ? EMPTY : str
                    .getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    public static String toString(byte[] data) {
        return toString(data, 0, data.length);
    }

    public static String toString(byte[] data, int off, int len) {
        try {
            return new String(data, off, len, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    private static String trim(String text, int off, int len) {
        while (len > 0 && text.charAt(off + len - 1) <= ' ') {
            len -= 1;
        }
        while (len > 0 && text.charAt(off) <= ' ') {
            len -= 1;
            off += 1;
        }
        return text.substring(off, off + len);
    }

    private final StreamFactory base;

    private final State state;

    public Store() throws IOException {
        this(System.getProperty("user.dir", "."));
    }

    public Store(File root) throws IOException {
        this.base = new StreamFactory.Local(new File(root, ".hg"));
        this.state = new State(base, root);
    }

    public Store(String cwd) throws IOException {
        this(findBase(cwd));
    }
    
    public void add(final String... names) throws LockFailed, InterruptedException {
        Lock lock = writeLock();
        try {
            for (String name : names) {
                File file = state.file(name);
                if (!file.isFile()) {
                    if (file.exists()) {
                        System.err.println("not a plain file: " + file);
                    } else {
                        System.err.println("doex not exist: " + file);
                    }
                } else {
                    byte s = state.getState(name);
                    if (s == 'a' || s == 'n') {
                        System.err.println("already tracked: " + file);
                    } else {
                        state.update((byte)'a', name);
                    }
                }
            }
        } finally {
            lock.release();
        }
    }

    public Changes changes() throws IOException {
        return new Changes(base);
    }

    public Element file(String path) throws IOException {
        return new Element(base, path);
    }

    /**
     * Loop up version for the given key, first in the current tags and then as
     * either generation or version hash.
     * 
     * @param key
     *            tag, generation or version hash.
     * @return a version, or null.
     * @throws IOException
     *             propagated from I/O.
     */
    public Version lookup(String key) throws IOException {
        Version result = tags().get(key);
        if (result == null) {
            result = changes().lookup(key);
        }
        return result;
    }

    public final Manifest manifest() throws IOException {
        return new Manifest(base);
    }

    public List<TagEntry> taglist() throws IOException {
        ArrayList<TagEntry> result = new ArrayList<TagEntry>();
        for (Map.Entry<String, Version> e : tags().entrySet()) {
            int g = -2;
            try {
                g = changes().generation(e.getValue());
            } catch (Exception unknown) {
                // ignored
            }
            result.add(new TagEntry(g, e.getKey(), e.getValue()));
        }
        Collections.sort(result);
        return result;
    }

    /**
     * @return a map from tag name to version.
     * @throws IOException
     *             propagated from I/O.
     */
    public HashMap<String, Version> tags() throws IOException {
        HashMap<String, Version> tags = new HashMap<String, Version>();
        final Element f = file(HG_TAGS);
        final List<Version> h = f.heads();
        Collections.reverse(h);
        for (Version v : h) {
            addTags(tags, new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(f.read(v)), "UTF-8")));
        }
        try {
            InputStream in = base.openInput("localtags");
            try {
                addTags(tags, new BufferedReader(new InputStreamReader(in)));
            } finally {
                in.close();
            }
        } catch (FileNotFoundException optional) {
            // ignored
        }
        tags.put(TIP, changes().tip());
        return tags;
    }

    public List<String> tags(Version v) throws IOException {
        List<String> result = new ArrayList<String>();
        for (Entry<String, Version> e : tags().entrySet()) {
            if (v.equals(e.getValue())) {
                result.add(e.getKey());
            }
        }
        return result;
    }

    public Undo transaction() throws IOException {
        final StreamFactory.Local localBase = (StreamFactory.Local) base;

        try {
            InputStream in = base.openInput(State.DIRSTATE);
            try {
                OutputStream out = base.openOutput("journal.dirstate");
                try {
                    final byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) != -1) {
                        if (n > 0) {
                            out.write(buffer, 0, n);
                        }
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            // not yet created
        }
        final File journal = localBase.file("journal");
        Runnable after = new Runnable() {

            public void run() {
                forceRename(journal, localBase.file("undo"));
                forceRename(localBase.file("journal.dirstate"), localBase
                        .file("undo.dirstate"));
            }

        };
        return new Undo(journal, after);
    }

    public Walker walk() {
        return state.walk();
    }

    public Lock writeLock() throws LockFailed, InterruptedException {
        Lock lock = new Lock(((StreamFactory.Local) base).file("wlock"),
                new Runnable() {
                    public void run() {
                        try {
                            state.write();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
        lock.lock(0);
        state.read();
        return lock;
    }

    public static int indexOf(final byte[] data, final byte[] match) {
        return indexOf(data, match, 0);
    }

    public static int indexOf(final byte[] data, final byte[] match,
            final int start) {
        final int end = data.length - match.length;
        seek: for (int i = start; i <= end; i++) {
            int j = match.length;
            while (--j >= 0) {
                if (data[i + j] != match[j]) {
                    continue seek;
                }
            }
            return i;
        }
        return -1;
    }

    public static int indexOf(final byte[] data, final int b, final int start) {
        for (int i = start; i < data.length; i++) {
            if (data[i] == b) {
                return i;
            }
        }
        return -1;
    }
}
