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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Working copy state.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class State {
    public static class Entry {
        public final String copy;

        public final int mode;

        public final int size;

        public final int time;

        public final String name;

        public final byte state;

        public Entry(final DataInputStream input) throws IOException {
            this.state = input.readByte();
            this.mode = input.readInt();
            this.size = input.readInt();
            this.time = input.readInt();
            int len = input.readInt();
            final byte[] data = new byte[len];
            input.readFully(data);
            final int mark = Util.indexOf(data, 0, 0);
            if (mark != -1) {
                name = Util.toString(data, 0, mark);
                copy = Util.toString(data, mark + 1, data.length - mark - 1);
            } else {
                name = Util.toString(data);
                copy = null;
            }
        }

        public Entry(final Entry entry, final String src) {
            this.state = entry.state;
            this.mode = entry.mode;
            this.size = entry.size;
            this.time = entry.time;
            this.name = entry.name;
            this.copy = src;
        }

        public Entry(final String name, final String copy) {
            this((byte) 'a', name, copy);
        }

        public Entry(final byte state, final String name, final String copy) {
            this.state = state;
            this.mode = 0;
            this.size = 0;
            this.time = 0;
            this.name = name;
            this.copy = copy;
        }

        public Entry(final byte state, final String name, final File file) {
            this.state = state;
            this.mode = Util.fakeMode(file);
            this.size = (int) file.length();
            this.time = (int) (file.lastModified() / 1000);
            this.name = name;
            this.copy = null;
        }

        public Entry(final byte state, final String name) {
            this.state = state;
            this.mode = 0;
            this.size = 0;
            this.time = 0;
            this.name = name;
            this.copy = null;
        }

        public Entry copy(final String src) {
            return new Entry(this, src);
        }

        public void write(final DataOutputStream output) throws IOException {
            output.writeByte(state);
            output.writeInt(mode);
            output.writeInt(size);
            output.writeInt(time);
            final byte[] nameBytes = Util.toBytes(name);
            final byte[] copyBytes = copy == null ? null : Util.toBytes(copy);
            final int len = nameBytes.length
                    + (copy == null ? 0 : copyBytes.length + 1);
            output.writeInt(len);
            output.write(nameBytes);
            if (copy != null) {
                output.writeByte(0);
                output.write(copyBytes);
            }
        }
    }

    private static class RepoFilter implements FilenameFilter {

        private final File root;

        public RepoFilter(final File root) {
            this.root = root;
        }

        public boolean accept(File dir, String name) {
            return dir != root || !name.equals(".hg");
        }

    }

    public static final String DIRSTATE = "dirstate";

    private static final String GLOB = "glob";

    private static final String RE = "re";

    private static final String REGEXP = "regexp";

    private static final String RELGLOB_ = "relglob:";

    private static final String RELRE_ = "relre:";

    private static final String SYNTAX_ = "syntax:";

    private final StreamFactory base;

    private boolean dirty;

    private Filter ignore;

    private HashMap<String, Entry> map;

    private Version[] parents;

    private final File root;

    State(final StreamFactory base, final File root) {
        this.base = base;
        this.root = root;
    }

    public void close() throws IOException {
        if (dirty) {
            write();
        }
    }

    public boolean contains(final String key) {
        return getMap().containsKey(key);
    }

    public String copied(String name) {
        final Entry entry = getMap().get(name);
        return entry == null ? null : entry.copy;
    }

    public void setCopy(final String src, final String dst) {
        final HashMap<String, Entry> map = getMap();
        final Entry entry = map.get(dst);
        this.dirty = true;
        if (entry == null) {
            map.put(dst, new Entry(dst, src));
        } else {
            map.put(entry.name, entry.copy(src));
        }
    }

    public File file(String name) {
        return new File(root, name);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public void forget(final String... names) {
        HashMap<String, Entry> map = getMap();
        for (final String name : names) {
            if (map.remove(name) == null) {
                // FIXME
                System.err.println("warning: not in state: '" + name + "'");
            } else {
                this.dirty = true;
            }
        }
    }

    public Entry get(final String key) {
        return getMap().get(key);
    }

    private final HashMap<String, Entry> getMap() {
        init();
        return map;
    }

    /**
     * @return the parents of the current working copy state.
     */
    public Version[] getParents() {
        init();
        return this.parents;
    }

    /**
     * @param name
     *            entry name.
     * @return the state of <var>name</var>, or '?'.
     */
    public byte getState(String name) {
        final Entry entry = getMap().get(name);
        return entry == null ? (byte) '?' : entry.state;
    }

    public Filter hgignore() {
        final Filter.Cascade result = new Filter.Cascade();
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(root, ".hgignore"))));
            try {
                String line;
                String syntax = RELRE_;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith(SYNTAX_)) {

                        String s = line.substring(SYNTAX_.length()).trim();
                        if (s.equals(RE) || s.equals(REGEXP)) {
                            syntax = RELRE_;
                        } else if (s.equals(GLOB)) {
                            syntax = RELGLOB_;
                        } else {
                            // FIXME
                            System.err.println("invalid syntax ignored: '" + s
                                    + "'");
                        }
                    } else {
                        if (!line.startsWith(RELRE_)
                                && !line.startsWith(RELGLOB_)) {
                            line = syntax + line;
                        }
                        try {
                            if (line.startsWith(RELRE_)) {
                                result.add(new Filter.Regexp(line
                                        .substring(RELRE_.length())));
                            } else if (line.startsWith(RELGLOB_)) {
                                result.add(new Filter.Glob(line
                                        .substring(RELGLOB_.length())));
                            } else {
                                throw new IllegalStateException();
                            }
                        } catch (Exception e) {
                            // FIXME
                            System.err.println("invalid pattern ignored: '"
                                    + line + "': " + e);
                        }
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            // ignore file is optional
        }
        return result;
    }

    public Filter ignore() {
        if (this.ignore == null) {
            this.ignore = hgignore();
        }
        return this.ignore;
    }

    /**
     * Read state from file if not already initialized.
     */
    public final void init() {
        if (map == null) {
            read();
        }
    }

    /**
     * Read state from file, if it exists.
     */
    public final void read() {
        try {
            final DataInputStream in = new DataInputStream(base
                    .openInput(DIRSTATE));
            try {
                final Version[] parents = new Version[2];
                parents[0] = Version.create(in);
                parents[1] = Version.create(in);
                final HashMap<String, Entry> map = new HashMap<String, State.Entry>();
                while (in.available() > 0) {
                    final Entry entry = new Entry(in);
                    map.put(entry.name, entry);
                }
                this.parents = parents;
                this.map = map;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            reset();
            return;
        }
    }

    public String relative(File file) {
        String path = file.getPath();
        String prefix = root.getPath() + File.separatorChar;
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        try {
            path = file.getCanonicalPath();
            prefix = root.getCanonicalPath();
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("not in tree: '" + file + "'");
    }

    /**
     * Reset this state to empty.
     */
    private void reset() {
        this.map = new HashMap<String, State.Entry>();
        this.parents = new Version[] { Version.NULL, Version.NULL };
    }

    public void setParents(final Version p1) {
        setParents(p1, Version.NULL);
    }

    public void setParents(final Version p1, final Version p2) {
        this.dirty = true;
        this.parents = new Version[] { p1, p2 };
    }

    public void update(final byte state, final String... names) {
        if (names == null || names.length == 0) {
            return;
        }
        HashMap<String, Entry> map = getMap();
        this.dirty = true;
        for (String name : names) {
            if (state == 'r') {
                map.put(name, new Entry(state, name));
            } else {
                File file = new File(root, name.toString());
                map.put(name, new Entry(state, name, file));
            }
        }
        // for f in files:
        // if state == "r":
        // self.map[f] = ('r', 0, 0, 0)
        // else:
        // s = os.lstat(self.wjoin(f))
        // st_size = kw.get('st_size', s.st_size)
        // st_mtime = kw.get('st_mtime', s.st_mtime)
        // self.map[f] = (state, s.st_mode, st_size, st_mtime)
        // if self.copies.has_key(f):
        // del self.copies[f]
    }

    public Walker walk() {
        return walk(new Walker.FilterWalker(new Walker.FileWalker(root,
                new RepoFilter(root)), new Filter.Inverted(hgignore())));
    }

    public Walker walk(final File... files) {
        return walk(new Walker.FilterWalker(new Walker.FileWalker(root,
                new RepoFilter(root), files), new Filter.Inverted(hgignore())));
    }

    private Walker walk(Walker fileWalker) {
        Walker mapWalker = new Walker.CollectionWalker(getMap().keySet());
        return new Walker.UniqueWalker(new Walker.JoinWalker(fileWalker,
                mapWalker));
    }

    /**
     * Write current state to file.
     * 
     * @throws IOException
     *             propagated from I/O.
     */
    public final void write() throws IOException {
        final DataOutputStream out = new DataOutputStream(base
                .openAtomic(DIRSTATE));
        try {
            for (int i = 0; i < parents.length; i++) {
                out.write(Version.id(parents[i]));
            }
            for (Entry entry : map.values()) {
                entry.write(out);
            }
            dirty = false;
        } finally {
            out.close();
        }
    }
}
