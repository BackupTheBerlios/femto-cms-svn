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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base 64 Encoding with URL and Filename Safe Alphabet as defined by RFC 3548.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class StateCodec extends FilterOutputStream {

    /**
     * Answer the size in characters that a block of <var>n</var> bytes will
     * occupy when encoded in base64.
     */
    public static final int base64Size(int n) {
        return (n * 8 + 5) / 6;
    }

    /**
     * Decode 6 bits of data from the given character.
     * 
     * @param b
     *            a character of base64 encoded data.
     */
    public static final int fromBase64(byte b) {
        return (b - (b < 48 ? -17 : (b < 65 ? -4 : (b < 95 ? 65 : (b < 97 ? 32
                : 71)))));
    }
    
    public static final byte[] fromBase64(final byte[] data) {
        byte[] result = new byte[(data.length * 6) / 8];
        int bits = 0, reg = 0, j = 0;
        for (int i = 0; i < data.length; i++) {
            reg = reg << 6 | fromBase64(data[i]);
            bits += 6;
            while (bits >= 8) {
                bits -= 8;
                result[j++] = (byte) (reg >>> bits);
            }
        }
        return result;
    }

    public static final byte toBase64(int n) {
        return (byte) ((n < 26 ? 65 : (n < 52 ? 71 : (n < 62 ? -4
                : (n < 63 ? -17 : 32)))) + n);
    }

    public static final void writeBase64(final OutputStream out,
            final byte[] data, final int off, final int len) throws IOException {
        int bits = 0, reg = 0;
        final int end = off + len;
        for (int i = off; i < end; i++) {
            reg = reg << 8 | (data[i] & 0xff);
            bits += 8;
            do {
                bits -= 6;
                out.write(toBase64((reg >>> bits) & 0x3f));
            } while (bits >= 6);
        }
        if (bits > 0) {
            out.write(toBase64((reg << (6 - bits)) & 0x3f));
        }
    }

    /**
     * Register to hold the bits not yet written. It contains a marker 1 bit one
     * position above the highest bit of data. Any any given time, this register
     * contains 0, 2 or 4 bits.
     */
    private int rest = 1;

    /**
     * Create a filter output stream that will encode data in base64. Be sure to
     * flush or close the stream so that the last bits will be flushed to the
     * underlying stream.
     * 
     * @param out
     *            underlying output stream, receiving base64 encoded output.
     */
    public StateCodec(OutputStream out) {
        super(out);
    }

    private final int bitsLeft() {
        final int n = rest;
        return n < 4 ? 0 : (n < 16 ? 2 : 4);
    }

    @Override
    public void flush() throws IOException {
        int bits = bitsLeft();
        if (bits > 0) {
            super.write(toBase64((rest << (6 - bits)) & 0x3f));
            rest = 1;
        }
        super.flush();
    }

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        final int end = off + len;
        int bits = bitsLeft(), reg = rest;
        for (int i = off; i < end; i++) {
            reg = reg << 8 | (data[i] & 0xff);
            bits += 8;
            do {
                bits -= 6;
                out.write(toBase64((reg >>> bits) & 0x3f));
            } while (bits >= 6);
        }
        int mask = 1 << bits;
        rest = (reg & (mask - 1)) | mask;
    }

    @Override
    public void write(int b) throws IOException {
        int bits = bitsLeft() + 8, reg = rest << 8 | (b & 0xff);
        do {
            bits -= 6;
            out.write(toBase64((reg >>> bits) & 0x3f));
        } while (bits >= 6);
        int mask = 1 << bits;
        rest = (reg & (mask - 1)) | mask;
    }
}
