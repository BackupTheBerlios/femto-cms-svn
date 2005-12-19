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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Load a list of Text constants from a bencoded data file.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class TextLoader extends Sink {

    public static final int LOADER_VERSION = 1;

    public static Iterator<Text> fromBencoded(final Class context) {
        return load(context, nameFor(context, ".data"));
    }

    public static Iterator<Text> fromXML(final Class context) {
        try {
            final TextLoader loader = new TextLoader();
            final String name = nameFor(context, ".data.xml");
            final InputStream in = context.getResourceAsStream(name);
            try {
                new Parser(Resolver.INSTANCE, loader).parse(in);
                return loader.getList();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Iterator<Text> load(final Class context, final String name) {
        try {
            InputStream in = context.getResourceAsStream(name);
            try {
                if (loadInt(in) > LOADER_VERSION) {
                    throw new IOException("file version exceeds loader version");
                }
                return loadList(in).iterator();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final static int loadInt(final InputStream in) throws IOException {
        if (in.read() != 'i') {
            throw new IOException("integer expected");
        } else {
            return readInt(in, 0, 'e');
        }
    }

    private final static ArrayList<Text> loadList(final InputStream in)
            throws IOException {
        if (in.read() != 'l') {
            throw new IOException("list expected");
        }
        int b;
        ArrayList<Text> result = new ArrayList<Text>();
        while ((b = in.read()) != -1) {
            if (b == 'e') {
                return result;
            } else if ('0' <= b && b <= '9') {
                int len = readInt(in, -(b - '0'), ':');
                result.add(readText(in, len));
            } else {
                break;
            }
        }
        throw new IOException("invalid list format: '" + (char) b + "'");
    }

    private static String nameFor(final Class context, final String suffix) {
        String name = context.getName();
        int dot = name.lastIndexOf('.');
        return name.substring(dot + 1) + suffix;
    }

    private final static int readInt(final InputStream in, int n, final int e)
            throws IOException {
        int b, s = -1;
        while ((b = in.read()) != -1) {
            if (b == '-') {
                if (n != 0 || s != -1) {
                    break;
                }
                s = 1;
            }
            if (b == e) {
                return n * s;
            } else if ('0' <= b && b <= '9') {
                n = n * 10 - (b - '0');
            }
        }
        throw new IOException("invalid number format: '" + (char) b + "'");
    }

    private final static Text readText(final InputStream in, final int len)
            throws IOException {
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

    private final ArrayList<Text> list = new ArrayList<Text>();

    private int TAG_TEXT;

    private boolean text;

    private TextLoader() {
    }

    public Iterator<Text> getList() {
        return list.iterator();
    }

    @Override
    public void handleCharacterData(boolean parsed, Text value) {
        if (text) {
            text = false;
            list.add(value);
        }
    }

    @Override
    public void handleCloseElement(int name) {
        if (text) {
            text = false;
            list.add(Text.EMPTY);
        }
    }

    @Override
    public void handleOpenDocument(NamePool<Text> l, NamePool<NamePair> q) {
        final int uri = l.intern(Text.valueOf(TextLoader.class.getName()));
        this.TAG_TEXT = Parser.nameFor(uri, Text.constant((byte) 't',
                (byte) 'e', (byte) 'x', (byte) 't'), q, l);
    }

    @Override
    public void handleOpenElement(int name) {
        text = name == TAG_TEXT;
    }

}
