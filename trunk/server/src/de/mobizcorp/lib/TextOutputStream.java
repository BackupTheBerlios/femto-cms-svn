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
import java.io.OutputStream;

/**
 * Output stream writing to a text sequence.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class TextOutputStream extends OutputStream implements TextSequence {

    private final TextBuffer buffer;

    public TextOutputStream() {
        this(new TextBuffer());
    }

    public TextOutputStream(int initialCapacity) {
        this(new TextBuffer(initialCapacity));
    }

    public TextOutputStream(TextBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException();
        }
        this.buffer = buffer;
    }

    public byte getByte(int index) {
        return buffer.getByte(index);
    }

    public int getUnicode(int index) {
        return buffer.getUnicode(index);
    }

    public TextIterator iterator() {
        return buffer.iterator();
    }

    public int length() {
        return buffer.length();
    }

    public Text part(int off, int len) {
        return buffer.part(off, len);
    }

    public int size() {
        return buffer.size();
    }

    public Text toText() {
        return buffer.toText();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buffer.append(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        buffer.append((byte) b);
    }

    public int writeTo(byte[] out, int off, int len) {
        return buffer.writeTo(out, off, len);
    }

    public void writeTo(OutputStream out) throws IOException {
        buffer.writeTo(out);
    }

}
