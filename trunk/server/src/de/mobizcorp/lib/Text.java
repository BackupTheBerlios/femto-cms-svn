/*
 * Plain UTF-8 API.
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
package de.mobizcorp.lib;

import java.io.IOException;
import java.io.InputStream;
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

    public static final int MAX_RADIX = 36;

    public static final int MIN_RADIX = 2;

    public static final Text TRUE = valueOf("true");

    public static final Text WHITE_SPACE = valueOf(" \f\t\r\n");

    public static Text constant(final byte... data) {
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

    public static final boolean equals(Text a, Text b) {
        return a == b || (a != null && a.equals(b));
    }

    static int getUnicode(final byte[] data, int i) {
        while ((data[i] & 0xC0) == 0x80) {
            --i;
        }
        int b = data[i];
        if ((b & 0x80) != 0) {
            int r = b;
            int m = 0x3F;
            while ((r & 0xC0) == 0xC0) {
                b = (b << 6) | (data[++i] & 0x3F);
                m = (m << 5) | 0x3F;
                r <<= 1;
            }
            b = b & m;
        }
        return b;
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
        } else {
            try {
                int scan = len;
                while (--scan >= 0) {
                    if (a[a0 + scan] != b[b0 + scan]) {
                        return false;
                    }
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        }
    }

    public static int toDigit(int b, int radix) {
        final int v;
        if (48 <= b && b <= 57) {
            v = b - 48;
        } else if (65 <= b && b <= 90) {
            v = b - 55;
        } else if (97 <= b && b <= 122) {
            v = b - 87;
        } else {
            throw new NumberFormatException("invalid character: " + (char) b);
        }
        if (v < radix) {
            return v;
        } else {
            throw new NumberFormatException("invalid digit: " + v
                    + " for radix " + radix);
        }
    }

    public static Text urlDecode(String str) {
        int pos = -1, count = 0;
        final int length = str.length();
        while ((pos = str.indexOf('%', pos + 1)) != -1) {
            if (pos < length - 2) {
                count += 1;
            } else {
                throw new IllegalArgumentException("truncated URL escape: '"
                        + str + "'");
            }
        }
        if (count == 0) {
            return valueOf(str);
        }
        pos = 0;
        final byte[] data = new byte[length - (count * 2)];
        for (int i = 0; i < length; i++) {
            int c = str.charAt(i);
            if (c == '%' && i < length - 2) {
                c = toDigit(str.charAt(++i), 16);
                c = c * 16 + toDigit(str.charAt(++i), 16);
            }
            if (c > 255) {
                throw new IllegalArgumentException("invalid character in url: "
                        + c);
            }
            data[pos++] = (byte) c;
        }
        return new Text(data, 0, data.length);
    }

    public static Text urlDecode(Text txt) {
        int pos = -1, count = 0;
        final int length = txt.size();
        while ((pos = txt.indexOf('%', pos + 1)) != -1) {
            if (pos < length - 2) {
                count += 1;
            } else {
                throw new IllegalArgumentException("truncated URL escape: '"
                        + txt + "'");
            }
        }
        if (count == 0) {
            return txt;
        }
        pos = 0;
        final int txtOff = txt.off;
        final byte[] txtData = txt.data;
        final int txtEnd = txtOff + length;
        final byte[] data = new byte[length - (count * 2)];
        for (int i = txt.off; i < txtEnd; i++) {
            int c = txtData[i];
            if (c == '%' && i < txtEnd - 2) {
                c = toDigit(txt.data[++i], 16);
                c = c * 16 + toDigit(txt.data[++i], 16);
            }
            if (c > 255) {
                throw new IllegalArgumentException("invalid character in url: "
                        + c);
            }
            data[pos++] = (byte) c;
        }
        return new Text(data, 0, data.length);
    }

    public static Text valueOf(byte[] data, int off, int len) {
        byte[] copy = new byte[len];
        System.arraycopy(data, off, copy, 0, len);
        return new Text(copy, 0, len);
    }

    public static Text valueOf(InputStream in, int len) throws IOException {
        final byte[] data = new byte[len];
        int off = 0;
        while (off < len) {
            final int n = in.read(data, off, len - off);
            if (n == -1) {
                throw new IOException("end of file in text");
            } else if (n > 0) {
                off += n;
            }
        }
        return Text.constant(data);
    }

    public static Text valueOf(int n, final int radix) {
        if (MIN_RADIX > radix || radix > MAX_RADIX) {
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

    public static Text valueOf(Object o) {
        if (o instanceof Text) {
            return (Text) o;
        } else {
            return new Text(String.valueOf(o));
        }
    }

    private final byte[] data;

    private final int off, len;

    Text(final byte[] data, final int off, final int len) {
        this.data = data;
        this.off = off;
        this.len = len;
    }

    public Text(final String str) {
        try {
            this.data = str.getBytes("UTF-8");
            this.len = data.length;
            this.off = 0;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public Text(final TextSequence... parts) {
        int pos = 0;
        for (final TextSequence part : parts) {
            pos += part.size();
        }
        this.data = new byte[pos];
        this.off = 0;
        this.len = pos;
        pos = 0;
        for (final TextSequence part : parts) {
            pos += part.writeTo(data, pos, part.size());
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

    public int getUnicode(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " < 0");
        }
        if (len <= index) {
            throw new IndexOutOfBoundsException(len + " <= " + index);
        }
        return getUnicode(data, off + index);
    }

    @Override
    public int hashCode() {
        return FNV1aHash.hash(data, off, len);
    }

    public int indexOf(final int b) {
        return indexOf(b, 0, len);
    }

    public int indexOf(final int b, final int off) {
        return indexOf(b, off, len);
    }

    public int indexOf(final int b, final int off, final int len) {
        final int end = off + (off + len > this.len ? this.len : len);
        for (int i = off; i < end; i++) {
            if (data[this.off + i] == b) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(final Text t) {
        return indexOf(t, 0, len);
    }

    public int indexOf(final Text t, final int off) {
        return indexOf(t, off, len);
    }

    public int indexOf(final Text t, final int off, final int len) {
        final int end = off + (off + len > this.len ? this.len : len) - t.len
                + 1;
        scan: for (int i = off; i < end; i++) {
            int j = t.len;
            while (--j >= 0) {
                if (data[this.off + i + j] != t.data[t.off + j]) {
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

    public int lastIndexOf(final int b) {
        return lastIndexOf(b, 0, len);
    }

    public int lastIndexOf(final int b, final int off) {
        return lastIndexOf(b, off, len);
    }

    public int lastIndexOf(final int b, final int off, final int len) {
        int i = off + len > this.len ? this.len : off + len;
        while (--i >= off) {
            if (data[this.off + i] == b) {
                return i;
            }
        }
        return -1;
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
            int j = t.len;
            while (--j >= 0) {
                if (data[this.off + i + j] != t.data[t.off + j]) {
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
        } else if (len < 0) {
            throw new NegativeArraySizeException(len + " < 0");
        }
        return new Text(data, this.off + off, len);
    }

    public boolean regionMatches(final int start, final byte[] data,
            final int off, final int len) {
        return regionMatches(this.data, this.off + start, data, off, len);
    }

    public Text replace(final byte a, final byte b) {
        int scan = off + len;
        while (--scan >= off) {
            if (data[scan] == a) {
                break;
            }
        }
        if (scan < off) {
            return this;
        }
        final byte[] temp = new byte[len];
        System.arraycopy(data, off, temp, 0, len);
        scan -= off;
        temp[scan] = b;
        while (--scan >= 0) {
            if (temp[scan] == a) {
                temp[scan] = b;
            }
        }
        return new Text(temp, 0, temp.length);
    }

    public int size() {
        return len;
    }

    public boolean startsWith(final Text t) {
        if (len < t.len) {
            return false;
        }
        byte[] da = data;
        int ia = off + t.len;
        byte[] db = t.data;
        int ib = t.off + t.len;
        while (--ia >= off) {
            if (da[ia] != db[--ib]) {
                return false;
            }
        }
        return true;
    }

    public boolean toBoolean() {
        return equals(TRUE);
    }

    public byte[] toBytes() {
        byte[] result = new byte[len];
        writeTo(result, 0, len);
        return result;
    }

    public int toInt(int radix) {
        int i = off, n = 0;
        final int end = off + len;
        final int sign;
        if (i < end && data[i] == '-') {
            sign = 1;
            i += 1;
        } else {
            sign = -1;
        }
        for (; i < end; i++) {
            n = n * radix - toDigit(data[i], radix);
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
