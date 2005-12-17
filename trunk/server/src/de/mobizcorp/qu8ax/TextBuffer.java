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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Mutable implementation of a text sequence. This class is intended to be used
 * analogous to the standard Java StringBuffer class.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public final class TextBuffer implements TextSequence {

    private static final byte[] EMPTY_BUFFER = new byte[0];

    public static TextBuffer valueOf(InputStream in) throws IOException {
        int n = in.available();
        return (n > 1 ? new TextBuffer(n) : new TextBuffer()).append(in);
    }

    private byte[] data;

    private int len;

    private int off;

    private boolean open;

    public TextBuffer() {
        this.data = EMPTY_BUFFER;
    }

    public TextBuffer(int initialCapacity) {
        this.data = new byte[initialCapacity];
        this.open = true;
    }

    public TextBuffer append(byte b) {
        extend(1, true);
        data[off + len++] = b;
        return this;
    }

    public TextBuffer append(byte[] data) {
        return append(data, 0, data.length);
    }

    public TextBuffer append(byte[] data, int off, int len) {
        extend(len, true);
        System.arraycopy(data, off, this.data, this.off + this.len, len);
        this.len += len;
        return this;
    }

    public TextBuffer append(InputStream in) throws IOException {
        int n;
        byte[] b = data;
        do {
            if (b.length == len) {
                int more = chunk(8192);
                b = new byte[b.length + more];
            }
            n = in.read(b, len, b.length - len);
            if (n > 0) {
                if (data != b) {
                    // Late copy: we only swap buffers after we know that
                    // the end of file was not yet reached. Saves one large
                    // copy for the last read. Also, if the buffer was pre-
                    // allocated with the exact file size, we can use the
                    // no-copy branch in toText() later.
                    System.arraycopy(data, off, b, off, data.length);
                    data = b;
                }
                len += n;
            }
        } while (n != -1);
        return this;
    }

    public TextBuffer append(int ucs4) {
        if (ucs4 < 0) {
            throw new IllegalArgumentException("negative code point");
        } else if (ucs4 < 0x80) {
            return append((byte) ucs4);
        } else if (ucs4 < 0x800) {
            // 110xxxxx 10xxxxxx
            extend(2, true);
            data[off + len++] = (byte) (0xC0 | ((ucs4 >>> 6) & 0x3F));
        } else if (ucs4 < 0x10000) {
            // 1110xxxx 10xxxxxx 10xxxxxx
            extend(3, true);
            data[off + len++] = (byte) (0xE0 | ((ucs4 >>> 12) & 0x3F));
            data[off + len++] = (byte) (0x80 | ((ucs4 >>> 6) & 0x3F));
        } else if (ucs4 < 0x110000) {
            // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            extend(4, true);
            data[off + len++] = (byte) (0xE0 | ((ucs4 >>> 18) & 0x3F));
            data[off + len++] = (byte) (0x80 | ((ucs4 >>> 12) & 0x3F));
            data[off + len++] = (byte) (0x80 | ((ucs4 >>> 6) & 0x3F));
        } else {
            throw new IllegalArgumentException("invalid code point: " + ucs4);
        }
        data[off + len++] = (byte) (0x80 | (ucs4 & 0x3F));
        return this;
    }

    public TextBuffer append(String str) {
        try {
            return append(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public TextBuffer append(TextSequence t) {
        int more = t.size();
        extend(more, true);
        len += t.writeTo(data, off + len, more);
        return this;
    }

    public void chop(int off, int len) {
        if (off < 0) {
            off = this.len < len ? 0 : this.len - len;
        }
        if (off + len > this.len) {
            len = this.len - off;
        }
        if (len <= 0) {
            return;
        }
        if (off + len == this.len) {
            this.len -= len;
        } else if (off == 0) {
            this.off += len;
        } else {
            this.len -= len;
            System.arraycopy(data, this.off + off + len, data, this.off + off,
                    this.len - off);
        }
    }

    /**
     * Calculate the buffer space to allocate to hold at least <var>amount</var>
     * more octets.
     * 
     * @param amount
     *            the number of bytes needed.
     * @return amount to allocate.
     */
    private int chunk(int amount) {
        int more = data.length / 2;
        return amount > more ? amount : more;
    }

    public void clear() {
        this.off = this.len = 0;
        if (!open) {
            this.data = EMPTY_BUFFER;
        }
    }

    @Override
    public boolean equals(Object other) {
        try {
            return equals((TextBuffer) other);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean equals(TextBuffer other) {
        if (this == other) {
            return true;
        } else if (other == null || this.len != other.len) {
            return false;
        } else {
            return Text.regionMatches(this.data, this.off, other.data,
                    other.off, this.len);
        }
    }

    private void extend(int amount, boolean atEnd) {
        byte[] grow;
        if (atEnd && off + len + amount > data.length) {
            int more = chunk(amount);
            grow = new byte[data.length + more];
            System.arraycopy(data, off, grow, off, len);
        } else if (!atEnd && off < amount) {
            int more = chunk(amount);
            int oldOff = off;
            grow = new byte[data.length + more];
            System.arraycopy(data, oldOff, grow, off += more, len);
        } else if (!open) {
            grow = new byte[data.length];
            if (len > 0) {
                System.arraycopy(data, off, grow, off, len);
            }
        } else {
            // open and space available.
            return;
        }
        data = grow;
        open = true;
    }

    public byte getByte(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " < 0");
        }
        if (len <= index) {
            throw new IndexOutOfBoundsException(len + " <= " + index);
        }
        return data[off + index];
    }

    protected byte[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return FNV1aHash.hash(data, off, len);
    }

    public TextIterator iterator() {
        // Prevent concurrent modification.
        return toText().iterator();
    }

    public int length() {
        return Text.length(data, off, len);
    }

    public Text part(int off, int len) {
        if (off + len > this.len) {
            throw new IndexOutOfBoundsException("[" + off + "," + (off + len)
                    + "[ not inside [0," + this.len + "[");
        } else {
            return Text.valueOf(data, this.off + off, len);
        }
    }

    public TextBuffer prepend(byte b) {
        extend(1, false);
        data[--off] = b;
        return this;
    }

    public TextBuffer prepend(byte[] data) {
        return prepend(data, 0, data.length);
    }

    public TextBuffer prepend(byte[] data, int off, int len) {
        extend(len, true);
        System.arraycopy(data, off, this.data, this.off -= len, len);
        this.len += len;
        return this;
    }

    public TextBuffer prepend(String str) {
        try {
            return prepend(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public TextBuffer prepend(TextSequence t) {
        int more = t.size();
        extend(more, false);
        if (t.writeTo(data, off - more, more) < more) {
            throw new IllegalStateException("short write");
        }
        off -= more;
        len += more;
        return this;
    }

    public int size() {
        return len;
    }

    @Override
    public String toString() {
        try {
            return new String(data, off, len, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public Text toText() {
        if (0 == off && data.length == len) {
            open = false;
            return new Text(data, off, len);
        } else {
            return Text.valueOf(data, off, len);
        }
    }

    public int writeTo(byte[] out, int off, int len) {
        int count = this.len < len ? this.len : len;
        System.arraycopy(this.data, this.off, out, off, count);
        return count;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(data, off, len);
    }
}
