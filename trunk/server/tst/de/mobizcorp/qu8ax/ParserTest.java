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

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;

import junit.framework.TestCase;
import de.mobizcorp.lib.Text;
import de.mobizcorp.qu8ax.WriterTest.XmlTest;

/**
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class ParserTest extends TestCase {

    public static class TraceOutput implements Handler {

        private final PrintWriter out;

        private NamePool<Text> lNames;

        private NamePool<NamePair> qNames;

        public TraceOutput(final PrintWriter out) {
            this.out = out;
        }

        public void handleAddAttribute(final int name, final Text value)
                throws IOException {
            NamePair pair = qNames.extern(name);
            out.println("handleAddAttribute:" + lNames.extern(pair.getA())
                    + ":" + lNames.extern(pair.getB()) + ":" + value);
        }

        public void handleCharacterData(final boolean parsed, final Text value)
                throws IOException {
            out.println("handleCharacterData:" + parsed + ":"
                    + URLEncoder.encode(value.toString(), "UTF8"));
        }

        public void handleCloseDocument() throws IOException {
            out.println("handleCloseDocument");
        }

        public void handleCloseElement(final int name) throws IOException {
            NamePair pair = qNames.extern(name);
            out.println("handleCloseElement:" + lNames.extern(pair.getA())
                    + ":" + lNames.extern(pair.getB()));
        }

        public void handleInstruction(final Text target, final Text value)
                throws IOException {
            out.println("handleInstruction:" + target + ":" + value);
        }

        public void handleOpenDocument(final NamePool<Text> lNames,
                final NamePool<NamePair> qNames) throws IOException {
            this.lNames = lNames;
            this.qNames = qNames;
            out.println("handleOpenDocument");
        }

        public void handleOpenElement(final int name) throws IOException {
            NamePair pair = qNames.extern(name);
            out.println("handleOpenElement:" + lNames.extern(pair.getA()) + ":"
                    + lNames.extern(pair.getB()));
        }

        public void handleStartElement(final int name) throws IOException {
            NamePair pair = qNames.extern(name);
            out.println("handleStartElement:" + lNames.extern(pair.getA())
                    + ":" + lNames.extern(pair.getB()));
        }

        public void handleWhitespace(final boolean comment, final Text value)
                throws IOException {
            out.println("handleWhitespace:" + comment + ":"
                    + URLEncoder.encode(value.toString(), "UTF8"));
        }
    }

    /*
     * Test method for 'de.mobizcorp.qu8ax.Parser.Parser(Resolver, Handler)'
     */
    public void testParser() throws IOException, IllegalAccessException,
            InvocationTargetException {
        final ArrayList<char[]> results = new ArrayList<char[]>();
        WriterTest.withList(new XmlTest() {
            public void runXmlTest(InputStream in) throws IOException {
                CharArrayWriter buffer = new CharArrayWriter();
                final PrintWriter printer = new PrintWriter(buffer);
                TraceOutput output = new TraceOutput(printer);
                Parser parser = new Parser(Resolver.INSTANCE, output);
                parser.parse(in);
                printer.close();
                results.add(buffer.toCharArray());
            }
        });
        BufferedReader l = WriterTest.openList();
        try {
            String line;
            int index = 0;
            while ((line = l.readLine()) != null) {
                final InputStream in = WriterTest.class
                        .getResourceAsStream(line + ".out");
                if (in == null) {
                    System.out.println(results.get(index++));
                    assertNotNull(in);
                }
                try {
                    BufferedReader sample = new BufferedReader(
                            new InputStreamReader(in, "UTF8"));
                    BufferedReader result = new BufferedReader(
                            new CharArrayReader(results.get(index++)));
                    while ((line = sample.readLine()) != null) {
                        assertEquals(line, result.readLine());
                    }
                    assertNull(result.readLine());
                } finally {
                    in.close();
                }
            }
        } finally {
            l.close();
        }
    }

}
