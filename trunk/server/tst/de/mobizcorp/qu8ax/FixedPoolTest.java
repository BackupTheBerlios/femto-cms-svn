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
public class FixedPoolTest extends TestCase {

    /*
     * Test method for 'de.mobizcorp.qu8ax.FixedPool.open()'
     */
    public final void testOpen() {
        StringTokenizer tok;
        final int[] ids1 = new int[5];
        final OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("Platinum\nGold\nSilver\nBronze\nTin", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids1[i] = pool.intern(tok.nextToken());
        }
        final FixedPool<String> fix = pool.close();
        final ChildPool<String> open = fix.open();
        final int[] ids2 = new int[4];
        tok = new StringTokenizer("one\ntwo\nthree\nfour", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids2[i] = open.intern(tok.nextToken());
        }
        for (int i = 0; i < ids1.length; i++) {
            for (int j = 0; j < ids2.length; j++) {
                assertFalse(ids1[i] == ids2[j]);
            }
        }
        tok = new StringTokenizer("Tin\nBronze\nSilver\nGold\nPlatinum", "\n");
        for (int i = ids1.length - 1; tok.hasMoreTokens(); i--) {
            final String token = tok.nextToken();
            assertTrue(open.contains(token));
            assertEquals(token, open.extern(ids1[i]));
            assertEquals(ids1[i], open.intern(token));
        }
        tok = new StringTokenizer("four\nthree\ntwo\none", "\n");
        for (int i = ids2.length - 1; tok.hasMoreTokens(); i--) {
            final String token = tok.nextToken();
            assertTrue(open.contains(token));
            assertEquals(token, open.extern(ids2[i]));
            assertEquals(ids2[i], open.intern(token));
        }
    }

}
