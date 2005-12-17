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

/**
 * Fowler/Noll/Vo hash function, see http://www.isthe.com/chongo/tech/comp/fnv/
 * for details on the algorithm. This class only implements FNV-1a for 32 bit
 * hash values to serve the standard java hashCode() method.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class FNV1aHash {

	/** The unsigned 2166136261 constant represented as Java int. */
	public static final int FNV32_OFFSET_BASIS = -2128831035;

	/** The magic FNV prime for 32 bit hashes. */
	public static final int FNV32_PRIME = 16777619;

	public static final int hash(byte[] data) {
		return hash(data, 0, data.length);
	}

	public static final int hash(byte[] data, int off, int len) {
		return next(FNV32_OFFSET_BASIS, data, off, len);
	}

	public static final int hash(int a, int b) {
		return next(next(FNV32_OFFSET_BASIS, a), b);
	}

	public static final int next(int hash, byte datum) {
		return (hash ^ (datum & 0xff)) * FNV32_PRIME;
	}

	public static final int next(int hash, byte[] data) {
		return next(hash, data, 0, data.length);
	}

	public static final int next(int hash, byte[] data, int off, int len) {
		final byte[] in = data;
		final int end = off + len;
		while (off < end) {
			hash = next(hash, in[off++]);
		}
		return hash;
	}
	
	public static final int next(int hash, short data) {
		return next(next(hash, (byte)(data >>> 8)), (byte)(data & 0xFF));
	}

	public static final int next(int hash, int data) {
		return next(next(hash, (short)(data >>> 16)), (short)(data & 0xFFFF));
	}

}
