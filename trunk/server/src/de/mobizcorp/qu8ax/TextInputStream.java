/*
 * Quick UTF-8 API for XML.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
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

package de.mobizcorp.qu8ax;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream reading from a text sequence.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class TextInputStream extends InputStream {

    /** Index of the next byte to read. */
    private int index = 0;

    /** Mark for next reset(). */
    private int mark = -1;

    /** Input text sequence. */
    private final TextSequence source;

    /**
     * Create a text input stream for the given source text sequence.
     * 
     * @param source
     *            a text sequence.
     */
    public TextInputStream(final TextSequence source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
    }

    @Override
    public int available() throws IOException {
        return source.size() - index;
    }

    @Override
    public synchronized void mark(final int readlimit) {
        mark = index;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        try {
            byte b = source.getByte(index);
            index += 1;
            return b;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public int read(final byte[] b, final int off, int len) throws IOException {
        if (len < 200) {
            // FIXME: this is an arbitrary trade-off between instance creation
            // and iterative copying.
            return super.read(b, off, len);
        }
        final int limit = source.size();
        if (index + len > limit) {
            len = limit - index;
            if (len < 1) {
                return -1;
            }
        }
        int n = source.part(index, len).writeTo(b, off, len);
        index += n;
        return n;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mark == -1) {
            throw new IOException("mark not set");
        }
        index = mark;
        mark = -1;
    }

    @Override
    public long skip(long n) throws IOException {
        final int limit = source.size();
        if (index + n > limit) {
            n = limit - index;
        }
        index += n;
        return n;
    }

}
