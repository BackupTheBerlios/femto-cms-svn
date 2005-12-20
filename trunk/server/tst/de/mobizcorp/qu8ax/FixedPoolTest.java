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
        int[] ids = new int[5];
        OpenPool<String> pool = new OpenPool<String>();
        tok = new StringTokenizer("Platinum\nGold\nSilver\nBronze\nTin", "\n");
        for (int i = 0; tok.hasMoreTokens(); i++) {
            ids[i] = pool.intern(tok.nextToken());
        }
        FixedPool<String> fix = pool.close();
        ChildPool<String> open = fix.open();
        tok = new StringTokenizer("Tin\nBronze\nSilver\nGold\nPlatinum", "\n");
        for (int i = ids.length - 1; tok.hasMoreTokens(); i--) {
            final String token = tok.nextToken();
            assertTrue(open.contains(token));
            assertEquals(token, open.extern(ids[i]));
            assertEquals(ids[i], open.intern(token));
        }
    }

}
