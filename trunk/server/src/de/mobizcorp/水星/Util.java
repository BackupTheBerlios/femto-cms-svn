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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Utility methods.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Util {

    public static class RuntimeIOException extends RuntimeException {
        private final IOException delegate;

        public RuntimeIOException(final IOException delegate) {
            this.delegate = delegate;
        }

        public IOException getIOException() {
            return delegate;
        }
    }

    private static final byte[] EMPTY = new byte[0];

    /**
     * Convert a hexadecimal string to its binary representation.
     * 
     * @param hex
     *            a hex string.
     * @return binary data.
     */
    public static byte[] bin(final String hex) {
        final int end = hex.length() / 2;
        final byte[] result = new byte[end];
        for (int i = 0, j = 0; i < end; i++) {
            int b = nibble(hex.charAt(j++));
            b = (b << 4) | nibble(hex.charAt(j++));
            result[i] = (byte) b;
        }
        return result;
    }

    /**
     * Compare two bytes, with semantics of java.util.Comparator.
     * 
     * @param a
     *            a byte.
     * @param b
     *            another byte.
     * @return an integer &lt; 0, == 0 or &gt; 0 when <var>a</var> is less,
     *         equal or greater than <var>b</var>.
     */
    public static int compare(byte a, byte b) {
        return compare(a & 0xff, b & 0xff);
    }

    /**
     * Compare two byte arrays, with semantics of java.util.Comparator.
     * 
     * @param a
     *            a byte array.
     * @param b
     *            another byte array.
     * @return an integer &lt; 0, == 0 or &gt; 0 when <var>a</var> is less,
     *         equal or greater than <var>b</var>.
     */
    public static int compare(final byte[] a, final byte[] b) {
        if (a != b) {
            if (b == null) {
                return 1;
            } else if (a == null) {
                return -1;
            }
            final int dl = compare(a.length, b.length);
            final int end = dl < 0 ? b.length : a.length;
            for (int i = 0; i < end; i++) {
                final int db = compare(a[i], b[i]);
                if (db != 0) {
                    return db;
                }
            }
            return dl;
        }
        return 0;
    }

    /**
     * Compare two integers, with semantics of java.util.Comparator.
     * 
     * @param a
     *            an integer.
     * @param b
     *            another integer.
     * @return an integer &lt; 0, == 0 or &gt; 0 when <var>a</var> is less,
     *         equal or greater than <var>b</var>.
     */
    public static int compare(final int a, final int b) {
        return a == b ? 0 : (a < b ? -1 : 1);
    }

    /**
     * Hierarchical string compare. This differs from the standard string
     * comparision in that the separator character is always treated as zero,
     * which serves to sort strings of hierarchical names with a well-defined
     * order.
     * 
     * @param a
     *            a hierarchical name.
     * @param b
     *            another hierarchical name.
     * @param separator
     *            the character which separates parent from child name.
     * @return an integer &lt; 0, == 0 or &gt; 0 when <var>a</var> is less,
     *         equal or greater than <var>b</var>.
     */
    public static int compare(final String a, final String b,
            final char separator) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        }
        final int la = a.length();
        final int lb = b.length();
        final int end = la > lb ? lb : la;
        for (int i = 0; i < end; i++) {
            final char ca = a.charAt(i);
            final char cb = b.charAt(i);
            if (ca != cb) {
                return (ca == separator ? 0 : ca) - (cb == separator ? 0 : cb);
            }
        }
        return la - lb;
    }

    /**
     * Convert an integer between 0 and 35 to a character representation. Note
     * that this method does no range checks.
     * 
     * @param n
     *            a small integer between 0 and 35 inclusive.
     * @return a character of the sets 0-9 or a-z.
     */
    public static char digit(final int n) {
        return (char) ((n < 10 ? '0' : 'a' - 10) + n);
    }

    /**
     * Fake a *nix style file mode from the given file. Only folders are marked
     * executable, and all three bit groups are equal.
     * 
     * @param file
     *            a file.
     * @return an integer mask in the -rwxrwxrwx fashion of *nix operating
     *         systems.
     */
    public static int fakeMode(final File file) {
        int mask = (file.canRead() ? 4 : 0) | (file.canWrite() ? 2 : 0)
                | (file.isDirectory() ? 1 : 0);
        return mask | (mask << 3) | (mask << 6);
    }

    /**
     * Force rename of file <var>from</var> to <var>to</var>. If a simple
     * rename fails, the rename is retried after removing the target file.
     * 
     * @param from
     *            old name.
     * @param to
     *            new name.
     * @throws IOException
     *             when the rename operation fails.
     */
    public static void forceRename(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            to.delete();
            if (!from.renameTo(to)) {
                throw new IOException("cannot rename '" + from + "' to '" + to
                        + "'");
            }
        }
    }

    /**
     * Convert binary data to its hexadecimal string representation.
     * 
     * @param data
     *            binary data.
     * @return a hexadecimal string representation of <var>data</var>.
     */
    public static String hex(final byte[] data) {
        final int end = data.length;
        StringBuffer buffer = new StringBuffer(end * 2);
        for (int i = 0; i < end; i++) {
            int b = data[i];
            buffer.append(digit((b >>> 4) & 15)).append(digit(b & 15));
        }
        return buffer.toString();
    }

    /**
     * Return the index of <var>match</var> in <var>data</var>.
     * 
     * @param data
     *            binary data.
     * @param match
     *            binary pattern to search.
     * @return index of <var>match</var>, or -1.
     */
    public static int indexOf(final byte[] data, final byte[] match) {
        return indexOf(data, match, 0);
    }

    /**
     * Return the index of <var>match</var> in <var>data</var> at or after
     * <var>start</var>.
     * 
     * @param data
     *            binary data.
     * @param match
     *            binary pattern to search.
     * @return index of <var>match</var>, or -1. The result is always equal or
     *         greater to start, or -1.
     */
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

    /**
     * Return the index of <var>b</var> in <var>data</var> at or after
     * <var>start</var>.
     * 
     * @param data
     *            binary data.
     * @param b
     *            a byte to search.
     * @return index of <var>b</var>, or -1. The result is always equal or
     *         greater to start, or -1.
     */
    public static int indexOf(final byte[] data, final int b, final int start) {
        for (int i = start; i < data.length; i++) {
            if (data[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Convert a character n the range of 0-9 or a-zA-Z to its numerical value
     * as a digit. Upper and lower case characters are treated equal.
     * 
     * @param c
     *            a character.
     * @return its numerical value as a digit.
     */
    public static int nibble(final char c) {
        if (c >= 'a') {
            if (c <= 'f') {
                return c - 'a' + 10;
            }
        }
        if (c >= 'A') {
            if (c <= 'F') {
                return c - 'A' + 10;
            }
        }
        if (c >= '0') {
            if (c <= '9') {
                return c - '0';
            }
        }
        throw new IllegalArgumentException("invalid digit: '" + c + "'");
    }

    /**
     * Read a line from the input stream, with standard *nix style comment
     * parsing. Empty lines are ignored, and trailing white space is stripped.
     * 
     * @param in
     *            an input stream.
     * @param buf
     *            a buffer to read the line into.
     * @return true iff the buffer holds a new line.
     * @throws IOException
     *             propagated from I/O.
     */
    public static boolean readLine(final InputStream in, final StringBuffer buf)
            throws IOException {
        buf.setLength(0);
        int end = 0;
        boolean quote = false;
        while (in.available() > 0) {
            final int b = in.read();
            if (b == -1) {
                // oops eof
                break;
            } else if (quote) {
                quote = false;
                end = buf.length() + 1;
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
                end = buf.length() + 1;
            }
            buf.append((byte) b);
        }
        if (end < buf.length()) {
            buf.setLength(end);
        }
        return buf.length() > 0;
    }

    /**
     * Replace occurences of <var>a</var> in <var>t</var> with <var>b</var>.
     * 
     * @param t
     *            an input string.
     * @param a
     *            the string to replace.
     * @param b
     *            the replacement for <var>a</var>.
     * @return the string with replacements applied.
     */
    public static String replace(String t, String a, String b) {
        int start = 0, mark;
        StringBuffer buffer = null;
        while ((mark = t.indexOf(a, start)) != -1) {
            if (buffer == null) {
                buffer = new StringBuffer(t.length() + b.length());
            }
            buffer.append(t.substring(start, mark)).append(b);
            start = mark + a.length();
        }
        return buffer == null ? t : buffer.toString();
    }

    /**
     * Answer true iff <var>data</var> starts with <var>match</var>.
     * 
     * @param data
     *            binary data.
     * @param match
     *            binary pattern.
     * @return boolean result.
     */
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

    /**
     * Convert a string to its UTF-8 representation. This is the same as
     * String.getBytes but without the checked exception.
     * 
     * @param str
     *            a string.
     * @return UTF-8 representation of <var>str</var>.
     */
    public static byte[] toBytes(String str) {
        try {
            return (str == null || str.length() == 0) ? EMPTY : str
                    .getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    /**
     * Convert an UTF-8 byte array to a string.
     * 
     * @param data
     *            UTF-8 bytes.
     * @return a string.
     */
    public static String toString(byte[] data) {
        return toString(data, 0, data.length);
    }

    /**
     * Convert a section of an UTF-8 byte array to a string. The converted
     * section starts at <var>off</var> and extends <var>len</var> bytes from
     * there.
     * 
     * @param data
     *            UTF-8 bytes.
     * @param off
     *            offset into <var>data</var>
     * @param len
     *            the length of the secion to convert.
     * @return a string.
     */
    public static String toString(byte[] data, int off, int len) {
        try {
            return new String(data, off, len, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    /**
     * Trim white space off a section of a string. This is the same as
     * String.subtring(off, off + len).trim() but without the intervening
     * instance.
     * 
     * @param text
     *            a string.
     * @param off
     *            offset into string.
     * @param len
     *            length of segment to trim.
     * @return the segment of lenght <var>len</var> characters starting at
     *         <var>off</var>, trimmed of leading and trailing white space.
     */
    public static String trim(String text, int off, int len) {
        while (len > 0 && text.charAt(off + len - 1) <= ' ') {
            len -= 1;
        }
        while (len > 0 && text.charAt(off) <= ' ') {
            len -= 1;
            off += 1;
        }
        return text.substring(off, off + len);
    }

}
