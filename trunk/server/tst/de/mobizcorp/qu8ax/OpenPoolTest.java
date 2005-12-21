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

import java.util.StringTokenizer;

import junit.framework.TestCase;

/**
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class OpenPoolTest extends TestCase {

    /*
     * Test method for 'de.mobizcorp.qu8ax.OpenPool.close()'
     */
    public final void testClose() {
        StringTokenizer tok;
        int[] ids = new int[4];
        OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("one\ntwo\nthree\nfour", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids[i] = pool.intern(tok.nextToken());
        }
        FixedPool<String> fix = pool.close();
        tok = new StringTokenizer("four\nthree\ntwo\none", "\n");
        for (int i = ids.length - 1; tok.hasMoreTokens(); i--) {
            assertEquals(ids[i], fix.intern(tok.nextToken()));
        }
    }

    /*
     * Test method for 'de.mobizcorp.qu8ax.NamePool.contains(T)'
     */
    public final void testContains() {
        StringTokenizer tok;
        OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("one\ntwo\nthree\nfour", "\n");
        while (tok.hasMoreTokens()) {
            pool.intern(tok.nextToken());
        }
        tok = new StringTokenizer("eno\nONE", "\n");
        while (tok.hasMoreTokens()) {
            assertFalse(pool.contains(tok.nextToken()));
        }
        tok = new StringTokenizer("four\nthree\ntwo\none", "\n");
        while (tok.hasMoreTokens()) {
            assertTrue(pool.contains(tok.nextToken()));
        }
    }

    /*
     * Test method for 'de.mobizcorp.qu8ax.NamePool.extern(int)'
     */
    public final void testExtern() {
        StringTokenizer tok;
        final int[] ids = new int[4];
        final OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("one\ntwo\nthree\nfour", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids[i] = pool.intern(tok.nextToken());
        }
        tok = new StringTokenizer("four\nthree\ntwo\none", "\n");
        for (int i = ids.length - 1; i >= 0; i--) {
            assertEquals(pool.extern(ids[i]), tok.nextToken());
        }
    }

    /*
     * Test method for 'de.mobizcorp.qu8ax.NamePool.intern(T)'
     */
    public final void testIntern() {
        StringTokenizer tok;
        final int[] ids = new int[4];
        final OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("one\ntwo\nthree\nfour", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids[i] = pool.intern(tok.nextToken());
        }
        for (int i = 0; i < ids.length; i++) {
            for (int j = ids.length - 1; j > i; j--) {
                assertFalse(ids[i] == ids[j]);
            }
        }
    }

}
