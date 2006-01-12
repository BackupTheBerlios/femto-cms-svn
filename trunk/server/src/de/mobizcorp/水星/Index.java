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

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import de.mobizcorp.lib.Text;

/**
 * Index file implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Index {

	/** Maximum size of index file that will be read into memory. */
	private static final int MMAP_THRESHOLD = 10000;

	private final String mode;

	private final MappedByteBuffer buffer;

	private final HashMap<Version, Entry> map = new HashMap<Version, Entry>();

	private final Entry index[];

	public static class Entry {
		public static final int SIZE = 4 * 4 + 3 * Version.ID_SIZE;

		public static final int ID_OFFSET = SIZE - Version.ID_SIZE;

		// Index format:
		// byte type purpose
		// 0-3 int offset
		// 4-7 int length
		// 8-11 int base generation
		// 12-15 int link generation
		// 16-35 hash 1st parent
		// 36-55 hash 2nd parent
		// 56-75 hash id
		public final int offset, length, base, link;

		public final Version p0, p1, id;

		public final int g;

		public Entry(final ByteBuffer buffer, final int g) {
			offset = buffer.getInt();
			length = buffer.getInt();
			base = buffer.getInt();
			link = buffer.getInt();
			p0 = Version.create(buffer);
			p1 = Version.create(buffer);
			id = Version.create(buffer);
			this.g = g;
		}

		public Entry(final DataInput input, final int g) throws IOException {
			offset = input.readInt();
			length = input.readInt();
			base = input.readInt();
			link = input.readInt();
			p0 = Version.create(input);
			p1 = Version.create(input);
			id = Version.create(input);
			this.g = g;
		}
	}

	public Index(final File file, final String mode) throws IOException {
		this.mode = mode;
		final RandomAccessFile accessFile = openIndex(file, mode);
		final long length = accessFile == null ? 0 : accessFile.length();
		if (length > Integer.MAX_VALUE) {
			// While we may still be able to create the index array, the
			// index
			// will not be mappable into memory (by definition of
			// FileChannel.map).
			throw new IOException("file too large: (" + length + ")" + file);
		}
		this.index = new Entry[(int) (length / Entry.SIZE)];
		final int end = index.length;
		if (end * Entry.SIZE != length) {
			throw new IOException("corrupted index (size): " + file);
		}
		if (length < MMAP_THRESHOLD) {
			try {
				this.buffer = null;
				for (int g = 0; g < end; g++) {
					add(new Entry(accessFile, g));
				}
			} finally {
				if (accessFile != null) {
					accessFile.close();
				}
			}
		} else {
			this.buffer = accessFile.getChannel().map(
					isWritable() ? MapMode.READ_WRITE : MapMode.READ_ONLY, 0,
					length);
		}
	}

	private RandomAccessFile openIndex(final File file, final String mode)
			throws IOException {
		RandomAccessFile result;
		try {
			result = new RandomAccessFile(file, mode);
		} catch (FileNotFoundException e) {
			if (isWritable()) {
				throw e; // should be able to create
			} else {
				result = null;
			}
		}
		return result;
	}

	public boolean isWritable() {
		return mode.indexOf('w') != -1;
	}

	public int size() {
		return index.length;
	}

	public boolean isEmpty() {
		return index.length == 0;
	}

	public boolean containsKey(final Object key) {
		try {
			return get(key) != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public Entry get(final int g) {
		Entry result = index[g];
		if (null == result && null != buffer) {
			result = fetch(g);
		}
		return result;
	}

	public Entry get(final Object key) {
		Entry result = map.get(key);
		if (null == result) {
			result = find(key);
		}
		return result;
	}

	private Entry find(final Object key) {
		if (null != buffer && key instanceof Version) {
			final Version version = (Version) key;
			final int end = index.length;
			final byte[] id = new byte[Version.ID_SIZE];
			for (int i = 0; i < end; i++) {
				if (null == index[i]) {
					buffer.position(i * Entry.SIZE + Entry.ID_OFFSET);
					buffer.get(id);
					if (version.isId(id)) {
						return fetch(i);
					}
				}
			}
		}
		throw new NoSuchElementException(key.toString());
	}

	private Entry fetch(final int g) {
		buffer.position(g * Entry.SIZE);
		return add(new Entry(buffer, g));
	}

	private Entry add(final Entry e) {
		map.put(e.id, index[e.g] = e);
		return e;
	}

	public Version version(int g) {
		return g < 0 ? Version.NULL : get(g).id;
	}

	public Version tip() {
		return version(size() - 1);
	}

	public int generation(Version version) {
		return Version.NULL == version ? -1 : get(version).g;
	}

	public int link(Version version) {
		return get(version).link;
	}

	public int offset(int g) {
		return get(g).offset;
	}

	public int length(int g) {
		return get(g).length;
	}

	public int end(int g) {
		Entry entry = get(g);
		return entry.offset + entry.length;
	}

	public int base(int g) {
		return get(g).base;
	}
	
	public boolean check(byte[] data, int g) {
		Entry entry = get(g);
		return Version.check(data, entry.id, entry.p0, entry.p1);
	}

	public HashSet<Version> reachable(Version version, Version stop) {
		HashSet<Version> set = new HashSet<Version>();
		set.add(version);
		return reachable(set, version, stop, stop == null ? 0
				: generation(stop));
	}

	private HashSet<Version> reachable(HashSet<Version> set, Version version,
			Version stop, int min) {
		if (Version.NULL != version && (stop == null || !stop.equals(version))) {
			Entry entry = get(version);
			if (entry.g >= min && set.add(version)) {
				reachable(set, entry.p0, stop, min);
				reachable(set, entry.p1, stop, min);
			}
		}
		return set;
	}

	public List<Version> heads() {
		return heads(Version.NULL);
	}

	public List<Version> heads(final Version start) {
		final HashSet<Version> reachable = new HashSet<Version>();
		reachable.add(start);
		final ArrayList<Version> heads = new ArrayList<Version>();
		heads.add(start);
		final int end = size();
		for (int g = generation(start) + 1; g < end; g++) {
			final Entry entry = get(g);
			if (reachable.contains(entry.p0)) {
				reachable.add(entry.id);
				heads.remove(entry.p0);
				heads.add(entry.id);
			}
			if (reachable.contains(entry.p1)) {
				reachable.add(entry.id);
				heads.remove(entry.p1);
				heads.add(entry.id);
			}
		}
		return heads;
	}

	public Collection<Version> children(final Version version) {
		final ArrayList<Version> result = new ArrayList<Version>();
		final int end = size();
		for (int g = generation(version) + 1; g < end; g++) {
			final Entry entry = get(g);
			if (version.equals(entry.p0) || version.equals(entry.p1)) {
				result.add(entry.id);
			}
		}
		return result;
	}

	public Version lookup(Text spec) {
		try {
			int g = spec.toInt(10);
			if (g < 0) {
				g = size() + g;
			}
			if (g >= 0 && g < size()) {
				return version(g);
			}
		} catch (NumberFormatException e) {
			// fall back to hex id
		}
		Entry entry = get(Version.create(spec));
		if (null == entry) {
			throw new NoSuchElementException(spec.toString());
		} else {
			return entry.id;
		}
	}

}
