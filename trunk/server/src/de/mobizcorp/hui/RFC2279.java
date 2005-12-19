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

    public static int read(InputStream in) throws IOException {
        int b = in.read() & 0xff;
        if (b <= 0x7f) {
            return b;
        } else if (b <= 0xdf) {
            return readParts(b & 0x1f, 1, in);
        } else if (b <= 0xef) {
            return readParts(b & 0x0f, 2, in);
        } else if (b <= 0xf7) {
            return readParts(b & 0x07, 3, in);
        } else if (b <= 0xfb) {
            return readParts(b & 0x03, 4, in);
        } else if (b <= 0xfd) {
            return readParts(b & 0x01, 5, in);
        } else {
            throw new IOException("invalid UTF-8 start byte: "
                    + Integer.toHexString(b));
        }
    }

    public static int readParts(int b, int n, InputStream in)
            throws IOException {
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
