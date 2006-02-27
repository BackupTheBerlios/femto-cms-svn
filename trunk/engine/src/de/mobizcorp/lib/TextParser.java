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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Simple parser for text in the manner of StringTokenizer.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class TextParser implements Iterator<Text> {

    /** Index to the end of the last character parsed. */
    private int index = -1;

    /** Value to parse. */
    private final Text value;

    /** Set of characters that are valid separators. */
    private final Text separator;

    /**
     * Create a text parser for the given <var>value</var> and set of
     * <var>separator</var>s.
     * 
     * @param value
     *            the value to parse.
     * @param separator
     *            set of separators.
     */
    public TextParser(Text value, Text separator) {
        this.value = value;
        this.separator = separator;
    }

    public boolean hasNext() {
        return index < value.size();
    }

    public Text next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int start = index + 1;
        outer: while (++index < value.size()) {
            byte b = value.getByte(index);
            if (b < 0xC0) {
                int scan = separator.size();
                match: while (--scan >= 0) {
                    if (b == separator.getByte(scan)) {
                        if (b > 0x7F) {
                            int i = 0;
                            do {
                                b = value.getByte(index + --i);
                                if (b != separator.getByte(--scan)) {
                                    continue match;
                                }
                            } while ((b & 0xC0) == 0x80);
                            return value.part(start, index + i - start);
                        }
                        break outer;
                    }
                }
            }
        }
        return value.part(start, index - start);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
