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
import java.util.List;

import de.mobizcorp.lib.FNV1aHash;

/**
 * Diff implementation. This is a Java port of the bdiff algorithm by Matt
 * Mackall <mpm@selenic.com>.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Diff {
	private static class Hunk {
		public int a1, a2, b1, b2;

		public Hunk(final int a1, final int a2, final int b1, final int b2) {
			this.a1 = a1;
			this.a2 = a2;
			this.b1 = b1;
			this.b2 = b2;
		}
	}

	private static class Line {
		public final byte[] b;

		public int h, len, n, e, l;

		public Line(final int h, final int len, final int n, final int e,
				final int l, final byte[] b) {
			this.h = h;
			this.len = len;
			this.n = n;
			this.e = e;
			this.l = l;
			this.b = b;
		}
	}

	public static byte[] bdiff(final byte[] a, final byte[] b) {
		final Line[] al = splitlines(a);
		final Line[] bl = splitlines(b);
		final List<Hunk> l = diff(al, bl);
		/* calculate length of output */
		int len = 0, la = 0, lb = 0;
		for (Hunk h : l) {
			if (h.a1 != la || h.b1 != lb)
				len += 12 + bl[h.b1].l - bl[lb].l;
			la = h.a2;
			lb = h.b2;
		}

		/* build binary patch */
		la = lb = 0;
		final byte[] result = new byte[len];
		ByteBuffer rb = ByteBuffer.wrap(result);
		for (Hunk h : l) {
			if (h.a1 != la || h.b1 != lb) {
				len = bl[h.b1].l - bl[lb].l;
				rb.putInt(al[la].l - al[0].l);
				rb.putInt(al[h.a1].l - al[0].l);
				rb.putInt(len);
				rb.put(bl[lb].b, bl[lb].l, len);
			}
			la = h.a2;
			lb = h.b2;
		}
		return result;
	}

	public static List<Hunk> blocks(final byte[] a, final byte[] b) {
		final Line[] al = splitlines(a);
		final Line[] bl = splitlines(b);
		return diff(al, bl);
	}

	private static boolean cmp(final byte[] a, final int a0, final byte[] b,
			final int b0, final int len) {
		for (int i = 0; i < len; i++) {
			if (a[a0 + i] != b[b0 + i]) {
				return true;
			}
		}
		return false;
	}

	private static boolean cmp(final Line a, final Line b) {
		return a != b
				&& (a.h != b.h || a.len != b.len || cmp(a.b, a.l, b.b, b.l,
						b.len));
	}

	private static List<Hunk> diff(final Line[] a, final Line[] b) {
		final List<Hunk> result = new ArrayList<Hunk>();
		equatelines(a, b);
		final int[] pos = new int[b.length * 2];
		recurse(a, b, pos, 0, a.length, 0, b.length, result);
		result.add(new Hunk(a.length, 0, b.length, 0));
		return result;
	}

	private static void equatelines(final Line[] a, final Line[] b) {
		final int bn = b.length, buckets = hashSize(bn);
		final int[] h = new int[buckets * 2];
		for (int i = 0; i <= buckets;) {
			h[i++] = -1;
			h[i++] = 0;
		}
		final int mask = (buckets - 1) << 1;
		for (int i = bn - 1; i >= 0; i--) {
			/* find the equivalence class */
			final int j = findslot(b[i], h, mask, b);

			/* add to the head of the equivalence class */
			b[i].n = h[j];
			b[i].e = j >>> 1;
			h[j] = i;
			h[j + 1]++; /* keep track of popularity */
		}
		/* compute popularity threshold */
		final int an = a.length, t = (bn >= 200) ? bn / 100 : bn + 1;
		/* match items in a to their equivalence class in b */
		for (int i = 0; i < an; i++) {
			/* find the equivalence class */
			final int j = findslot(a[i], h, mask, b);

			a[i].e = j >>> 1; /* use equivalence class for quick compare */
			if (h[j + 1] <= t) {
				a[i].n = h[j]; /* point to head of match list */
			} else {
				a[i].n = -1; /* too popular */
			}
		}
	}

	private static int findslot(final Line l, final int[] h, final int mask,
			final Line[] b) {
		int j = (l.h * 2) & mask;
		while (h[j] != -1) {
			if (!cmp(l, b[h[j]])) {
				break;
			} else {
				j = (j + 2) & mask;
			}
		}
		return j;
	}

	private static int hashSize(final int n) {
		int result = 2;
		while (result < n + 1) {
			result *= 2;
		}
		return result;
	}

	private static int[] longest_match(final Line[] a, final Line[] b,
			final int[] pos, int a1, int a2, int b1, int b2) {
		int mi = a1, mj = b1, mk = 0, mb = 0, k;

		for (int i = a1; i < a2; i++) {
			int j = a[i].n;
			/* skip things before the current block */
			while (j != -1 && j < b1) {
				j = b[j].n;
			}

			/* loop through all lines match a[i] in b */
			for (; j != -1 && j < b2; j = b[j].n) {
				/* does this extend an earlier match? */
				if (i > a1 && j > b1 && pos[(j - 1) * 2] == i - 1) {
					k = pos[(j - 1) * 2 + 1] + 1;
				} else {
					k = 1;
				}
				pos[j * 2] = i;
				pos[j * 2 + 1] = k;

				/* best match so far? */
				if (k > mk) {
					mi = i;
					mj = j;
					mk = k;
				}
			}
		}

		if (mk != 0) {
			mi = mi - mk + 1;
			mj = mj - mk + 1;
		}

		/* expand match to include neighboring popular lines */
		while (mi - mb > a1 && mj - mb > b1
				&& a[mi - mb - 1].e == b[mj - mb - 1].e) {
			mb++;
		}
		while (mi + mk < a2 && mj + mk < b2 && a[mi + mk].e == b[mj + mk].e) {
			mk++;
		}

		return mk + mb == 0 ? null : new int[] { mi - mb, mj - mb, mk + mb };

	}

	private static void recurse(final Line[] a, final Line[] b,
			final int[] pos, final int a1, final int a2, final int b1,
			final int b2, final List<Hunk> l) {

		/* find the longest match in this chunk */
		int[] t = longest_match(a, b, pos, a1, a2, b1, b2);
		if (t == null)
			return;
		final int i = t[0], j = t[1], k = t[2];
		/* and recurse on the remaining chunks on either side */
		recurse(a, b, pos, a1, i, b1, j, l);
		l.add(new Hunk(i, i + k, j, j + k));
		recurse(a, b, pos, i + k, a2, j + k, b2, l);
	}

	private static Line[] splitlines(final byte[] a) {
		int n = 0;
		final int len = a.length;
		for (int p = 0; p < len; p++) {
			if (a[p] == '\n' || p == len - 1) {
				n++;
			}
		}
		final Line result[] = new Line[n];
		int b = 0, h = FNV1aHash.FNV32_OFFSET_BASIS;
		for (int p = 0, i = 0; p < len; p++) {
			h = FNV1aHash.next(h, a[p]);
			if (a[p] == '\n' || p == len - 1) {
				result[i++] = new Line(h, p - b + 1, -1, 0, b, a);
				b = p + 1;
				h = FNV1aHash.FNV32_OFFSET_BASIS;
			}
		}
		return result;
	}
}
