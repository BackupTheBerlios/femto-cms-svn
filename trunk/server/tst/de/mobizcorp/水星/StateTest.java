/*
 * 水星 - Water Star.
 * Copyright(C) 2006 Klaus Rennecke, all rights reserved.
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
package de.mobizcorp.水星;

import java.io.FileInputStream;

import de.mobizcorp.lib.TextBuffer;
import junit.framework.TestCase;

/**
 * State tests.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class StateTest extends TestCase {

    /*
     * Test method for 'de.mobizcorp.水星.State.State(File)'
     */
    public void testState() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.copied(Text)'
     */
    public void testCopied() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.init()'
     */
    public void testInit() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.parents()'
     */
    public void testParents() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.read()'
     */
    public void testRead() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.state(Text)'
     */
    public void testState1() {

    }

    /*
     * Test method for 'de.mobizcorp.水星.State.readLine(InputStream, TextBuffer)'
     */
    public void testReadLine() {
        try {
            // FIXME use proper fixture
            final TextBuffer buffer = new TextBuffer();
            final FileInputStream in = new FileInputStream("../hg/.hgignore");
            try {
                while (State.readLine(in, buffer)) {
                    System.out.println(buffer);
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
