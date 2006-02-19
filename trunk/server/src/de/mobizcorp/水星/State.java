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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;

/**
 * Working copy state.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class State {
    private static class Entry {
        public final Text copy;

        public final int i0;

        public final int i1;

        public final int i2;

        public final Text name;

        public final byte state;

        public Entry(final DataInputStream input) throws IOException {
            this.state = input.readByte();
            this.i0 = input.readInt();
            this.i1 = input.readInt();
            this.i2 = input.readInt();
            int len = input.readInt();
            Text t = Text.valueOf(input, len);
            final int mark = t.indexOf(0);
            if (mark != -1) {
                name = t.part(0, mark);
                copy = t.part(mark + 1, t.size() - mark - 1);
            } else {
                name = t;
                copy = null;
            }
        }

        public void write(final DataOutputStream output) throws IOException {
            output.writeByte(state);
            output.writeInt(i0);
            output.writeInt(i1);
            output.writeInt(i2);
            int len = name.size() + (copy == null ? 0 : copy.size() + 1);
            output.writeInt(len);
            name.writeTo(output);
            if (copy != null) {
                output.writeByte(0);
                copy.writeTo(output);
            }
        }
    }

    static boolean readLine(final InputStream in, final TextBuffer buf)
            throws IOException {
        buf.clear();
        int end = 0;
        boolean quote = false;
        while (in.available() > 0) {
            final int b = in.read();
            if (b == -1) {
                // oops eof
                break;
            } else if (quote) {
                quote = false;
                end = buf.size() + 1;
            } else if (b == '\\') {
                quote = true;
                continue;
            } else if (b == '\n') {
                if (end > 0) {
                    break;
                } else {
                    // skip empty lines.
                    continue;
                }
            } else if (b == '#') {
                int skip;
                while ((skip = in.read()) != -1 && skip != '\n') {
                    // skip rest of this line
                }
                if (end > 0) {
                    break;
                } else {
                    // skip empty lines.
                    continue;
                }
            } else if (b != ' ' && b != '\f' && b != '\t' && b != '\r') {
                end = buf.size() + 1;
            }
            buf.append((byte) b);
        }
        if (end < buf.size()) {
            buf.chop(end, buf.size() - end);
        }
        return buf.size() > 0;
    }

    private HashMap<Text, Entry> map;

    private Version[] parents;

    private final File root;

    private boolean dirty;

    State(final File root) {
        this.root = root;
    }

    public Text copied(Text name) {
        init();
        final Entry entry = map.get(name);
        return entry == null ? null : entry.copy;
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
     * @return the parents of the current working copy state.
     */
    public Version[] parents() {
        init();
        return this.parents;
    }

    /**
     * Read state from file, if it exists.
     */
    public final void read() {
        try {
            final DataInputStream in = new DataInputStream(new FileInputStream(
                    new File(root, "dirstate")));
            try {
                final Version[] parents = new Version[2];
                parents[0] = Version.create(in);
                parents[1] = Version.create(in);
                final HashMap<Text, Entry> map = new HashMap<Text, State.Entry>();
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

    /**
     * Reset this state to empty.
     */
    private void reset() {
        this.map = new HashMap<Text, State.Entry>();
        this.parents = new Version[] { Version.NULL, Version.NULL };
    }

    /**
     * @param name
     *            entry name.
     * @return the state of <var>name</var>, or '?'.
     */
    public byte state(Text name) {
        init();
        final Entry entry = map.get(name);
        return entry == null ? (byte) '?' : entry.state;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (dirty) {
                write();
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Write current state to file.
     * 
     * @throws IOException
     *             propagated from I/O.
     */
    public final void write() throws IOException {
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(
                new File(root, "dirstate")));
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
