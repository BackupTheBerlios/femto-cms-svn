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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patch implementation. This is a Java port of the patch algorithm by Matt
 * Mackall <mpm@selenic.com>.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Patch {
	public static final class Fragment {
		private byte[] data;

		private int end;

		private int start;

		public Fragment(ByteBuffer input) {
			start = input.getInt();
			end = input.getInt();
			int len = input.getInt();
			input.get(data = new byte[len]);
		}

		private Fragment(int start, int end, byte[] data) {
			this.start = start;
			this.end = end;
			this.data = data;
		}

		public int change() {
			return start + data.length - end;
		}

		public int chop(int pos) {
			int end = pos;
			if (this.end < end) {
				end = this.end;
			}
			int len = pos - this.start;
			if (data.length < len) {
				len = data.length;
			}
			return chop(end, len);
		}

		private int chop(final int end, final int len) {
			final int change = this.start + len - end;
			this.start = end;
			if (len != 0) {
				byte[] old = this.data;
				this.data = new byte[old.length - len];
				System.arraycopy(old, len, this.data, 0, old.length - len);
			}
			return change;
		}

		public int getEnd() {
			return end;
		}

		public int getLength() {
			return data.length;
		}

		public int getStart() {
			return start;
		}

		public Fragment move(int start, int end) {
			return new Fragment(start, end, data);
		}

		public Fragment split(int pos) {
			int end = pos;
			if (this.end < end) {
				end = this.end;
			}
			int len = pos - this.start;
			if (data.length < len) {
				len = data.length;
			}
			byte[] data = len != this.data.length ? new byte[len] : this.data;
			if (data != this.data) {
				System.arraycopy(this.data, 0, data, 0, len);
			}
			Fragment result = new Fragment(this.start, end, data);
			chop(end, len);
			return result;
		}

		public int writeTo(byte[] buffer, int offset) {
			System.arraycopy(data, 0, buffer, offset, data.length);
			return data.length;
		}

	}

	private static byte[] apply(final byte[] orig, final List<Fragment> l) {
		if (l == null || l.size() == 0) {
			return orig;
		}
		final byte[] result = new byte[calcsize(orig.length, l)];
		int last = 0;
		int p = 0;

		for (Fragment f : l) {
			final int old = f.getStart() - last;
			System.arraycopy(orig, last, result, p, old);
			p += old;
			p += f.writeTo(result, p);
			last = f.getEnd();
		}
		System.arraycopy(orig, last, result, p, orig.length - last);
		return result;
	}

	private static int calcsize(final int len, final List<Fragment> l) {
		int outlen = 0, last = 0;
		for (Fragment f : l) {
			outlen += f.getStart() - last;
			last = f.getEnd();
			outlen += f.getLength();
		}
		return outlen - last + len;
	}

	private static List<Fragment> combine(List<Fragment> a, List<Fragment> b) {
		int offset = 0;
		List<Fragment> c = new ArrayList<Fragment>();
		for (Fragment f : b) {

			offset = gather(c, a, f.getStart(), offset);
			int next = discard(a, f.getEnd(), offset);
			c.add(f.move(f.getStart() - offset, f.getEnd() - next));
			offset = next;
		}
		c.addAll(a);
		return c;
	}

	private static List<Fragment> decode(ByteBuffer input) {
		final List<Fragment> result = new ArrayList<Fragment>();
		while (input.remaining() > 0) {
			result.add(new Fragment(input));
		}
		return result;
	}

	private static int discard(List<Fragment> src, int cut, int offset) {
		return gather(null, src, cut, offset);
	}

	private static List<Fragment> fold(byte[][] bins, int start, int end) {
		if (start + 1 == end) {
			byte[] in = bins[start];
			if (in == null) {
				return Collections.emptyList();
			} else {
				return decode(ByteBuffer.wrap(in));
			}
		} else {
			int pivot = (end - start) / 2;
			return combine(fold(bins, start, start + pivot), fold(bins, start
					+ pivot, end));
		}
	}

	private static int gather(List<Fragment> dest, List<Fragment> src, int cut,
			int offset) {
		final int count = src.size();
		int i = 0;
		while (i < count) {
			Fragment f = src.get(i);
			if (f.getStart() + offset >= cut) {
				break;
			}
			if (offset + f.getStart() + f.getLength() <= cut) {
				offset += f.change();
				if (dest != null) {
					dest.add(f);
				}
				i++;
			} else {
				if (dest != null) {
					f = f.split(cut - offset);
					offset += f.change();
					dest.add(f);
				} else {
					offset += f.chop(cut - offset);
				}
				break;
			}
		}
		while (--i >= 0) {
			src.remove(0);
		}
		return offset;
	}

	public static byte[] patches(byte[] orig, byte[][] bins) {
		final int len = bins == null ? 0 : bins.length;
		if (len == 0) {
			return orig;
		} else {
			return apply(orig, fold(bins, 0, len));
		}
	}

	private Patch() {
	}
}
