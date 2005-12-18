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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Immutable implementation of a text sequence. This class is intended to be
 * used analogous to the standard Java String class.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public final class Text implements TextSequence {

    public static final Text EMPTY = constant();

    public static Text constant(byte... data) {
        return new Text(data, 0, data.length);
    }

    public static String dump(String message, byte[] data, int off, int len,
            int pos) {
        StringBuffer b = new StringBuffer();
        b.append(message).append(": ");
        dump(b, data, off, len, pos);
        return b.toString();
    }

    public static void dump(StringBuffer b, byte[] data, int off, int len,
            int pos) {
        if (pos < 0) {
            b.append("pos=").append(pos);
        }
        b.append("[");
        try {
            if (off > 0) {
                b.append("... ");
            }
            for (int i = off; i < off + len && b == b.append(", "); i++) {
                if (i < 0) {
                    b.append("off=").append(off);
                    i = -1;
                    continue;
                }
                int c = data[i] & 0xFF;
                if (i == pos) {
                    b.append(">");
                }
                if (0x20 <= c && c < 0x7F) {
                    b.append("'").append((char) c).append("'");
                } else {
                    String hex = Integer.toHexString(c);
                    b.append(hex.length() < 2 ? "0x0" : "0x").append(hex);
                }
                if (i == pos) {
                    b.append("<");
                }
            }
            if (off + len < data.length) {
                b.append(" ...");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            b.append("end=").append(off + len);
        }
        b.append("]");
        if (pos >= off + len) {
            b.append("pos=").append(pos);
        }
    }

    public static final int length(byte[] data, int off, int len) {
        int count = 0;
        int scan = off + len;
        while (--scan >= off) {
            if ((data[scan] & 0xFF) < 0xC0) {
                count += 1;
            }
        }
        return count;
    }

    public static final boolean regionMatches(byte[] a, int a0, byte[] b,
            int b0, int len) {
        if (a == b && a0 == b0) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else
            try {
                for (int i = 0; i < len; i++) {
                    if (a[a0 + i] != b[a0 + i]) {
                        return false;
                    }
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
    }

    public static Text valueOf(byte[] data, int off, int len) {
        byte[] copy = new byte[len];
        System.arraycopy(data, off, copy, 0, len);
        return new Text(copy, 0, len);
    }
    
    public static Text valueOf(int n, final int radix) {
        if (2 > radix || radix > 36) {
            throw new IllegalArgumentException("invalid radix: " + radix);
        }
        int c;
        if (n < 0) {
            c = 2;
        } else {
            n = -n;
            c = 1;
        }
        int r = -radix;
        // If we overflow r, we exceed Integer.MAX_VALUE
        while (r < 0 && r >= n) {
            r *= radix;
            c += 1;
        }
        final byte buffer[] = new byte[c];
        int pos = buffer.length;
        while (n <= -radix) {
            int m = n % radix;
            buffer[--pos] = (byte) ((m > -10 ? 48 : 55) - m);
            n /= radix;
        }
        buffer[--pos] = (byte) ((n > -10 ? 48 : 55) - n);
        if (pos > 0) {
            buffer[--pos] = 45;
        }
        return new Text(buffer, 0, buffer.length);
    }

    public static Text valueOf(String str) {
        return new Text(str);
    }
    
    private final byte[] data;

    private final int off, len;

    Text(byte[] data, int off, int len) {
        this.data = data;
        this.off = off;
        this.len = len;
    }

    public Text(String str) {
        try {
            this.data = str.getBytes("UTF-8");
            this.len = data.length;
            this.off = 0;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean endsWith(final Text t) {
        return lastIndexOf(t, len - t.len, 1) == 0;
    }

    @Override
    public boolean equals(Object other) {
        try {
            return equals((Text) other);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean equals(Text other) {
        if (this == other) {
            return true;
        } else if (other == null || this.len != other.len) {
            return false;
        } else {
            return regionMatches(this.data, this.off, other.data, other.off,
                    this.len);
        }
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

    @Override
    public int hashCode() {
        return FNV1aHash.hash(data, off, len);
    }

    public int indexOf(final Text t) {
        return indexOf(t, 0, len);
    }

    public int indexOf(final Text t, final int off) {
        return indexOf(t, off, len);
    }

    public int indexOf(final Text t, final int off, final int len) {
        final int end = (off + len > this.len ? this.len : len) - t.len;
        scan: for (int i = off; i < end; i++) {
            int j = i + t.len;
            while (--j >= i) {
                if (data[this.off + j] != t.data[t.off + j]) {
                    continue scan;
                }
            }
            return i;
        }
        return -1;
    }

    public TextIterator iterator() {
        return new TextIterator(data, off, len);
    }

    public int lastIndexOf(final Text t) {
        return lastIndexOf(t, 0, len);
    }

    public int lastIndexOf(final Text t, final int off) {
        return lastIndexOf(t, off, len);
    }

    public int lastIndexOf(final Text t, final int off, final int len) {
        int i = off + len > this.len - t.len + 1 ? this.len - t.len + 1 : off
                + len;
        scan: while (--i >= off) {
            int j = i + t.len;
            while (--j >= i) {
                if (data[this.off + j] != t.data[t.off + j]) {
                    continue scan;
                }
            }
            return i;
        }
        return -1;
    }

    public int length() {
        return length(data, off, len);
    }

    public Text part(int off, int len) {
        if (off + len > this.len) {
            throw new IndexOutOfBoundsException(this.len + " < " + (off + len));
        }
        return new Text(data, this.off + off, len);
    }

    public int size() {
        return len;
    }

    public boolean startsWith(final Text t) {
        return indexOf(t, 0, 1) == 0;
    }
    
    public int toInt(int radix) {
        int i = off, n = 0;
        final int end = off + len;
        final int sign;
        if (i < end && data[i] == 48) {
            sign = 1;
            i += 1;
        } else {
            sign = -1;
        }
        for (; i < end; i++) {
            final byte b = data[i];
            final int v;
            if (48 <= b && b <= 57) {
                v = b - 48;
            } else if (65 <= b && b <= 90) {
                v = b - 55;
            } else if (97 <= b && b <= 122) {
                v = b - 87;
            } else {
                throw new NumberFormatException("invalid character: " + (char)b);
            }
            if (b < radix) {
                n = n * radix + v;
            } else {
                throw new NumberFormatException("invalid digit: " + v + " for radix " + radix);
            }
        }
        return n * sign;
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
        return this;
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
