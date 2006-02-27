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
    public static final class Hunk {
        public final int leftStart, leftLimit, rightStart, rightLimit;

        public Hunk(final int leftStart, final int leftLimit,
                final int rightStart, final int rightLimit) {
            this.leftStart = leftStart;
            this.leftLimit = leftLimit;
            this.rightStart = rightStart;
            this.rightLimit = rightLimit;
        }

        public Hunk(final int leftMatch, final int rightMatch, final int length) {
            this(leftMatch, leftMatch + length, rightMatch, rightMatch + length);
        }

        private static Hunk longestMatch(final Line[] left, final Line[] right,
                final int[] pos, final int[] len, int leftStart, int leftLimit,
                int rightStart, int rightLimit) {
            int hunkLen = 0;
            final int hunkLeft, hunkRight;
            {
                int maxLeft = leftStart, maxRight = rightStart;

                for (int leftLine = leftStart; leftLine < leftLimit; leftLine++) {
                    int rightLine = left[leftLine].next;
                    /* skip things before the current block */
                    while (rightLine != -1 && rightLine < rightStart) {
                        rightLine = right[rightLine].next;
                    }

                    /* loop through all lines match left[leftPos] in right */
                    while (rightLine != -1 && rightLine < rightLimit) {
                        /* does this extend an earlier match? */
                        final int newLen;
                        if (leftLine > leftStart && rightLine > rightStart
                                && pos[rightLine - 1] == leftLine - 1) {
                            newLen = len[rightLine - 1] + 1;
                        } else {
                            newLen = 1;
                        }
                        pos[rightLine] = leftLine;
                        len[rightLine] = newLen;

                        /* best match so far? */
                        if (newLen > hunkLen) {
                            maxLeft = leftLine;
                            maxRight = rightLine;
                            hunkLen = newLen;
                        }
                        rightLine = right[rightLine].next;
                    }
                }

                if (hunkLen != 0) {
                    hunkLeft = maxLeft - hunkLen + 1;
                    hunkRight = maxRight - hunkLen + 1;
                } else {
                    hunkLeft = maxLeft;
                    hunkRight = maxRight;
                }
            }

            /* expand match to include neighboring lines */
            int prefix = 0;
            while (hunkLeft - prefix > leftStart
                    && hunkRight - prefix > rightStart
                    && left[hunkLeft - prefix - 1].slot == right[hunkRight
                            - prefix - 1].slot) {
                prefix++;
            }
            while (hunkLeft + hunkLen < leftLimit
                    && hunkRight + hunkLen < rightLimit
                    && left[hunkLeft + hunkLen].slot == right[hunkRight
                            + hunkLen].slot) {
                hunkLen++;
            }

            final int length = hunkLen + prefix;
            if (length == 0) {
                return null;
            }
            return new Hunk(hunkLeft - prefix, hunkRight - prefix, length);
        }

        public static List<Hunk> diff(final Line[] left, final Line[] right) {
            Line.equate(left, right);
            final int[] pos = new int[right.length];
            final int[] len = new int[right.length];
            final List<Hunk> result = new ArrayList<Hunk>();
            recurse(left, right, pos, len, 0, left.length, 0, right.length,
                    result);
            result.add(new Hunk(left.length, 0, right.length, 0));
            return result;
        }

        private static void recurse(final Line[] left, final Line[] right,
                final int[] pos, final int[] len, final int leftStart,
                final int leftLimit, final int rightStart,
                final int rightLimit, final List<Hunk> hunks) {

            /* find the longest match in this chunk */
            Hunk hunk = Hunk.longestMatch(left, right, pos, len, leftStart,
                    leftLimit, rightStart, rightLimit);
            if (hunk == null)
                return;
            /* and recurse on the remaining chunks on either side */
            recurse(left, right, pos, len, leftStart, hunk.leftStart,
                    rightStart, hunk.rightStart, hunks);
            hunks.add(hunk);
            recurse(left, right, pos, len, hunk.leftLimit, leftLimit,
                    hunk.rightLimit, rightLimit, hunks);
        }
    }

    private static final class Line {
        /** Data for this line. */
        public final byte[] data;

        /** Hash value over this line. */
        public final int hash;

        /** Offset and length in data. */
        public final int offset, length;

        /** Equivalence class - slot in hash table. */
        public int slot = 0;

        /** Next line in same equivalence class. */
        public int next = -1;

        public Line(final int hash, final byte[] data, final int offset,
                final int length) {
            this.hash = hash;
            this.length = length;
            this.offset = offset;
            this.data = data;
        }

        /**
         * Split data into lines, computing their hash as we go.
         * 
         * @param data
         *            binary data.
         * @return an array of lines from <var>data</var>.
         */
        public static Line[] split(final byte[] data) {
            int size = 0;
            final int limit = data.length - 1;
            for (int scan = 0; scan <= limit; scan++) {
                if (data[scan] == '\n' || scan == limit) {
                    size++;
                }
            }
            final Line result[] = new Line[size];
            int mark = 0, hash = FNV1aHash.FNV32_OFFSET_BASIS;
            for (int scan = 0, i = 0; scan <= limit; scan++) {
                hash = FNV1aHash.next(hash, data[scan]);
                if (data[scan] == '\n' || scan == limit) {
                    final int end = scan + 1;
                    result[i++] = new Line(hash, data, mark, end - mark);
                    mark = end;
                    hash = FNV1aHash.FNV32_OFFSET_BASIS;
                }
            }
            return result;
        }

        public static void equate(final Line[] left, final Line[] right) {
            new Table(right).match(left, threshold(right.length));
        }

        private static int threshold(final int length) {
            return (length >= 200) ? length / 100 : length + 1;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return equals((Line) obj);
            } catch (ClassCastException e) {
                return false;
            }
        }

        public boolean equals(final Line other) {
            if (this == other) {
                return true;
            } else if (this.hash != other.hash || this.length != other.length) {
                return false;
            } else {
                int scan = this.length;
                final byte[] a = this.data, b = other.data;
                final int a0 = this.offset, b0 = other.offset;
                while (--scan >= 0) {
                    if (a[a0 + scan] != b[b0 + scan]) {
                        return false;
                    }
                }
                return true;
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static final class Table {
        private final int[] index, count;

        private final Line[] lines;

        public Table(final Line[] lines) {
            // allocate one more for mismatches in left
            final int size = hashSize(lines.length + 1);
            this.lines = lines;
            this.index = new int[size];
            this.count = new int[size];
            fill();
        }

        private void fill() {
            final int[] index = this.index;
            int scan = index.length;
            while (--scan >= 0) {
                index[scan] = -1;
            }
            final Line[] lines = this.lines;
            final int[] count = this.count;
            scan = lines.length;
            while (--scan >= 0) {
                final Line line = lines[scan];
                final int slot = findSlot(line);
                line.next = index[slot];
                line.slot = slot;
                index[slot] = scan;
                count[slot]++;
            }
        }

        private static int hashSize(final int minimum) throws OutOfMemoryError {
            int scan = 1;
            while (scan < minimum) {
                if ((scan <<= 1) == 0) {
                    throw new OutOfMemoryError("minimum: " + minimum);
                }
            }
            return scan;
        }

        public void match(final Line[] left, final int threshold) {
            final int length = left.length;
            for (int scan = 0; scan < length; scan++) {
                final Line line = left[scan];
                final int slot = findSlot(line);
                line.slot = slot;
                if (count[slot] <= threshold) {
                    // point to head of match list
                    line.next = index[slot];
                } else {
                    // too popular
                    line.next = -1;
                }
            }
        }

        private int findSlot(final Line line) {
            final int[] index = this.index;
            final Line[] lines = this.lines;
            final int mask = index.length - 1;
            int scan = line.hash & mask;
            while (index[scan] != -1 && !line.equals(lines[index[scan]])) {
                scan = (scan + 1) & mask;
            }
            return scan;
        }
    }

    /**
     * @param left
     *            data.
     * @param right
     *            data.
     * @return binary difference between the left and right data.
     */
    public static byte[] bdiff(final byte[] left, final byte[] right) {
        final Line[] leftLines = Line.split(left);
        final Line[] rightLines = Line.split(right);
        return encode(leftLines, rightLines, Hunk.diff(leftLines, rightLines));
    }

    /**
     * @param left
     *            data.
     * @param right
     *            data.
     * @return list of hunks that match from left to right data.
     */
    public static List<Hunk> blocks(final byte[] left, final byte[] right) {
        final Line[] leftLines = Line.split(left);
        final Line[] rightLines = Line.split(right);
        return Hunk.diff(leftLines, rightLines);
    }

    private static byte[] encode(final Line[] left, final Line[] right,
            final List<Hunk> hunks) {
        final byte[] result = new byte[bdiffSize(right, hunks)];
        final ByteBuffer rb = ByteBuffer.wrap(result);
        int leftPos = 0, rightPos = 0;
        for (Hunk h : hunks) {
            if (h.leftStart != leftPos || h.rightStart != rightPos) {
                final int length = right[h.rightStart].offset
                        - right[rightPos].offset;
                rb.putInt(left[leftPos].offset - left[0].offset);
                rb.putInt(left[h.leftStart].offset - left[0].offset);
                rb.putInt(length);
                rb.put(right[rightPos].data, right[rightPos].offset, length);
            }
            leftPos = h.leftLimit;
            rightPos = h.rightLimit;
        }
        return result;
    }

    /**
     * Calculate the size of a binary diff for <var>lines</var> and <var>hunks</var>.
     * 
     * @param lines
     *            lines from right side.
     * @param hunks
     *            pre-computed diff hunks.
     * @return the size of the resulting bdiff.
     */
    private static int bdiffSize(final Line[] lines, final List<Hunk> hunks) {
        int result = 0, la = 0, lb = 0;
        for (Hunk h : hunks) {
            if (h.leftStart != la || h.rightStart != lb)
                result += 12 + lines[h.rightStart].offset - lines[lb].offset;
            la = h.leftLimit;
            lb = h.rightLimit;
        }
        return result;
    }
}
