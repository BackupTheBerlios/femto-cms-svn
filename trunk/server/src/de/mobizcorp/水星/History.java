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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.zip.DataFormatException;


/**
 * Abstract history implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class History {
    private static final byte[] EMPTY = new byte[0];

    private final String chunks;

    private RandomAccessFile dataFile;

    private final Index index;

    private final StreamFactory base;

    protected History(final StreamFactory base, final String index,
            final String chunks) throws IOException {
        this.base = base;
        this.index = new Index(base, index, "r");
        this.chunks = chunks;
    }

    public byte[] chunk(int g) throws IOException {
        final Index.Entry entry = index.get(g);
        byte[] buffer = new byte[entry.length];
        final int offset = entry.offset;
        if (base instanceof StreamFactory.Local) {
            readChunk(((StreamFactory.Local) base).file(chunks), buffer, offset);
        } else {
            readChunk(base.openInput(chunks), buffer, offset);
        }
        try {
            return Store.decompress(buffer);
        } catch (DataFormatException e) {
            throw new IOException("failed to decompress: " + e);
        }
    }

    private void readChunk(final InputStream in, final byte[] buffer,
            final int offset) throws IOException {
        int scan = offset;
        while (scan > 0) {
            long n = in.skip(offset);
            if (n > 0) {
                scan -= n;
            } else {
                throw new EOFException("EOF before chunk");
            }
        }
        scan = 0;
        while (scan < buffer.length) {
            int n = in.read(buffer, scan, buffer.length - scan);
            if (n > 0) {
                scan += n;
            } else {
                throw new EOFException("EOF in chunk");
            }
        }
    }

    private void readChunk(final File chunks, final byte[] buffer,
            final int offset) throws FileNotFoundException, IOException {
        if (dataFile == null) {
            dataFile = new RandomAccessFile(chunks, "r");
        }
        dataFile.seek(offset);
        dataFile.readFully(buffer);
    }

    public byte[] contents(Version version) throws IOException {
        if (Version.NULL == version) {
            return EMPTY;
        }
        final int g = index.generation(version);
        final int base = index.base(g);
        final byte[] orig = chunk(base);
        if (base == g) {
            return orig;
        }
        byte[][] bins = new byte[g - base][];
        for (int r = base + 1, j = 0; r <= g; r++) {
            bins[j++] = chunk(r);
        }
        byte[] text = Patch.patches(orig, bins);
        if (index.check(text, g)) {
            return text;
        }
        throw new IOException("integrity check failed on " + chunks + ":" + g);
    }

    public byte[] delta(Version version) throws IOException {
        int g = index.generation(version);
        int b = index.base(g);
        return g != b ? chunk(g) : Diff.bdiff(contents(index.version(g - 1)),
                contents(version));
    }

    public int generation(Version v) {
        return index.generation(v);
    }

    public List<Version> heads() {
        return index.heads();
    }

    public Version lookup(String key) {
        return index.lookup(key);
    }

    public Version tip() {
        return index.tip();
    }

    public Version version(int g) {
        return index.version(g);
    }
}
