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

import de.mobizcorp.lib.FNV1aHash;

/**
 * A pair of names, as identifiers from a name pool.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class NamePair {

    private final int a, b;

    public NamePair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        try {
            NamePair pair = (NamePair) other;
            return this.a == pair.a && this.b == pair.b;
        } catch (Exception e) {
            return false;
        }
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public int hashCode() {
        return FNV1aHash.hash(a, b);
    }

    @Override
    public String toString() {
        return "{" + a + "," + b + "}";
    }

}
