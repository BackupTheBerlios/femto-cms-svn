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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import de.mobizcorp.qu8ax.WriterTest.XmlTest;

import junit.framework.TestCase;

/**
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class FilterTest extends TestCase {

    /*
     * Test method for 'de.mobizcorp.qu8ax.Filter.Filter(Handler)'
     */
    public final void testFilter() throws IOException, IllegalAccessException,
            InvocationTargetException {
        final ArrayList<byte[]> samples = new ArrayList<byte[]>();
        WriterTest.withList(new XmlTest() {
            public void runXmlTest(InputStream in) throws IOException {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                Writer writer = new Writer(buffer);
                Parser parser = new Parser(Resolver.INSTANCE, writer);
                parser.parse(in);
                buffer.close();
                samples.add(buffer.toByteArray());
            }
        });
        final ArrayList<byte[]> results = new ArrayList<byte[]>();
        WriterTest.withList(new XmlTest() {
            public void runXmlTest(InputStream in) throws IOException {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                Writer writer = new Writer(buffer);
                Filter filter = new Filter(writer);
                Parser parser = new Parser(Resolver.INSTANCE, filter);
                parser.parse(in);
                buffer.close();
                results.add(buffer.toByteArray());
            }
        });
        int scan = samples.size();
        assertEquals(scan, results.size());
        while (--scan >= 0) {
            assertTrue(Arrays.equals(samples.get(scan), results.get(scan)));
        }
    }

}
