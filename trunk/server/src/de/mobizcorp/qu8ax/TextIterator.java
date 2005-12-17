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
 * Iterator over a text sequence. Note that the index is not identical to the
 * position in data; getIndex() and setIndex() are expensive operations.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public final class TextIterator {

	/**
	 * All methods that return a Unicode code point use this value to signal
	 * that the boundary of this iterator was reached.
	 */
	public static final int STOP = -1;

	private byte[] data;

	private int len;

	private int off;

	/**
	 * Position in data, always points at last octet of current character.
	 */
	private int pos;

	/**
	 * The Unicode code point of the current character.
	 */
	private int ucp;

	TextIterator(byte[] data, int off, int len) {
		this.data = data;
		this.off = off;
		this.len = len;
		this.pos = off - 1;
	}

	public int back() {
		if (pos < off) {
			return STOP;
		}
		while (--pos >= off && (data[pos] & 0xFF) > 0xBF)
			;
		if (pos < off) {
			return STOP;
		}
		while (--pos >= off && (data[pos] & 0xFF) > 0xBF)
			;
		return convert();
	}

	private int convert() {
		if (++pos >= off + len) {
			pos = off + len;
			return STOP;
		}
		int b = data[pos] & 0xFF;
		switch (b >>> 4) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			// 0xxx xxxx - ASCII equivalent range
			return this.ucp = b;
		case 8:
		case 9:
		case 10:
		case 11:
			// 10xx xxxx - continuation
			throw new IllegalStateException(
					dump("continuation code in the middle"));
		case 12:
		case 13:
			// 110x xxxx
			return this.ucp = shift(b & 0x1F);
		case 14:
			// 1110 xxxx
			return this.ucp = shift(shift(b & 0x0F));
		case 15:
			// 1111 0xxx
			if ((b & 0x08) != 0) {
				break;
			}
			return this.ucp = shift(shift(shift(b & 0x07)));
		}
		throw new IllegalStateException(dump("invalid code"));
	}

	public int current() {
		return ucp;
	}

	private String dump(String message) {
		return Text.dump(message, data, off, len, pos);
	}

	public int first() {
		pos = off - 1;
		return next();
	}

	public int getIndex() {
		return Text.length(data, off, pos - off + 1);
	}

	public int last() {
		pos = off + len;
		return back();
	}

	public int next() {
		return convert();
	}

	public void setIndex(int n) {
		first();
		while (--n >= 0) {
			next();
		}
	}

	private int shift(int bits) {
		if (++pos >= off + len) {
			throw new IllegalStateException(dump("incomplete code"));
		}
		byte b = data[pos];
		if ((b & 0xC0) != 0x80) {
			throw new IllegalStateException(dump("not a continuation"));
		}
		return (bits << 6) | (b & 0x3F);
	}
}
