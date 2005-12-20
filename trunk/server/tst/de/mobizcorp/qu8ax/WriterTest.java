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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class WriterTest extends TestCase {

    public static interface XmlTest {
        public void runXmlTest(InputStream in) throws IOException;
    }

    /*
     * Test method for 'de.mobizcorp.qu8ax.Writer.Writer(OutputStream)'
     */
    public void testWriter() throws IOException, IllegalAccessException,
            InvocationTargetException {
        final ArrayList<String> run1 = new ArrayList<String>();
        withList(new XmlTest() {
            public void runXmlTest(InputStream in) throws IOException {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                Writer writer = new Writer(buffer);
                Parser parser = new Parser(Resolver.INSTANCE, writer);
                parser.parse(in);
                buffer.close();
                run1.add(new String(buffer.toByteArray(), "UTF8"));
            }
        });
        int scan;
        final ArrayList<String> run2 = new ArrayList<String>();
        for (scan = 0; scan < run1.size(); scan++) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Writer writer = new Writer(buffer);
            Parser parser = new Parser(Resolver.INSTANCE, writer);
            ByteArrayInputStream in = new ByteArrayInputStream(run1.get(scan)
                    .getBytes("UTF8"));
            try {
                parser.parse(in);
                buffer.close();
                run2.add(new String(buffer.toByteArray(), "UTF8"));
            } finally {
                in.close();
            }
        }
        scan = run1.size();
        assertEquals(scan, run2.size());
        while (--scan >= 0) {
            assertEquals(run1.get(scan), run2.get(scan));
        }
    }

    public static void withList(XmlTest test) throws IOException,
            IllegalAccessException, InvocationTargetException {
        BufferedReader l = openList();
        try {
            String line;
            while ((line = l.readLine()) != null) {
                final InputStream in = WriterTest.class
                        .getResourceAsStream(line);
                try {
                    test.runXmlTest(in);
                } finally {
                    in.close();
                }
            }
        } finally {
            l.close();
        }
    }

    public static BufferedReader openList() throws IOException {
        return new BufferedReader(new InputStreamReader(WriterTest.class
                .getResourceAsStream("xml-suite.txt"), "UTF8"));
    }

}
