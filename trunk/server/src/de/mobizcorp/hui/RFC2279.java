/*
 * Half User Interface.
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
package de.mobizcorp.hui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * RFC2279 library. This was the old, *outdated* UTF-8 standard, which provides
 * encoding for values up to 7FFFFFFF. RFC3629, which is current, defines only
 * codes up to 10FFFF.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class RFC2279 {
    protected RFC2279() {
        // library class
    }

    /**
     * Write the unicode code point <var>n</var> encoded as RFC2279 UTF-8 to
     * <var>out</var>.
     * 
     * @param n
     *            unicode character, 0 <= n <= Integer.MAX_VALUE.
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
    public static void write(final int n, final OutputStream out)
            throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative value: " + n);
        } else if (n <= 0x7f) {
            // 0xxxxxxx
            out.write(n & 0x7f);
        } else if (n <= 0x7ff) {
            // 110xxxxx 10xxxxxx
            out.write((0xc0 | (n >>> 6)) & 0xdf);
            out.write((0x80 | n) & 0xbf);
        } else if (n <= 0xffff) {
            // 1110xxxx 10xxxxxx 10xxxxxx
            out.write((0xe0 | (n >>> 12)) & 0xef);
            out.write((0x80 | (n >>> 6)) & 0xbf);
            out.write((0x80 | n) & 0xbf);
        } else if (n <= 0x1fffff) {
            // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            out.write((0xf0 | (n >>> 18)) & 0xf7);
            out.write((0x80 | (n >>> 12)) & 0xbf);
            out.write((0x80 | (n >>> 6)) & 0xbf);
            out.write((0x80 | n) & 0xbf);
        } else if (n <= 0x3ffffff) {
            // 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            out.write((0xf8 | (n >>> 24)) & 0xfb);
            out.write((0x80 | (n >>> 18)) & 0xbf);
            out.write((0x80 | (n >>> 12)) & 0xbf);
            out.write((0x80 | (n >>> 6)) & 0xbf);
            out.write((0x80 | n) & 0xbf);
        } else {
            // 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
            out.write((0xfc | (n >>> 30)) & 0xfd);
            out.write((0x80 | (n >>> 24)) & 0xbf);
            out.write((0x80 | (n >>> 18)) & 0xbf);
            out.write((0x80 | (n >>> 12)) & 0xbf);
            out.write((0x80 | (n >>> 6)) & 0xbf);
            out.write((0x80 | n) & 0xbf);
        }
    }

    /**
     * Read the next unicode code point from <var>in</var>, encoded as RFC2279
     * UTF-8.
     * 
     * @param in
     *            an input stream.
     * @return the unicode character.
     * @throws IOException
     *             propagated from I/O.
     */
    public static int read(final InputStream in) throws IOException {
        int b = in.read() & 0xff, n;
        if (b <= 0x7f) {
            return b;
        } else if (b <= 0xdf) {
            b &= 0x1f;
            n = 1;
        } else if (b <= 0xef) {
            b &= 0x0f;
            n = 2;
        } else if (b <= 0xf7) {
            b &= 0x07;
            n = 3;
        } else if (b <= 0xfb) {
            b &= 0x03;
            n = 4;
        } else if (b <= 0xfd) {
            b &= 0x01;
            n = 5;
        } else if (b == 0xff) {
            return -1; // end of file
        } else {
            throw new IOException("invalid UTF-8 start byte: "
                    + Integer.toHexString(b));
        }
        while (--n >= 0) {
            int c = in.read() & 0xff;
            if ((c & 0xc0) != 0x80) {
                throw new IOException("invalid UTF-8 sequence byte: "
                        + Integer.toHexString(c));
            } else {
                b = b << 6 | (c & 0x3f);
            }
        }
        return b;
    }
}
