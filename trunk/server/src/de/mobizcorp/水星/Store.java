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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.lib.TextParser;

/**
 * Local SCM store.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Store {

	public static class TagEntry implements Comparable<TagEntry> {
		public final int g;

		public final Text t;

		public final Version v;

		private transient CollationKey ck;

		public TagEntry(final int g, final Text t, final Version v) {
			this.g = g;
			this.t = t;
			this.v = v;
		}

		public int compareTo(TagEntry other) {
			return this.g == other.g ? this.getCollationKey().compareTo(
					other.getCollationKey()) : this.g < other.g ? -1 : 1;
		}

		private synchronized CollationKey getCollationKey() {
			if (ck == null) {
				ck = getCollator().getCollationKey(t.toString());
			}
			return ck;
		}

		private static Collator c;

		private static synchronized Collator getCollator() {
			if (c == null) {
				c = Collator.getInstance();
			}
			return c;
		}
	}

	private static final Text HG_TAGS = Text.valueOf(".hgtags");

	// TODO find proper place for this
	public static final Text NL = Text.constant((byte) '\n');

	private static final Deflater sharedDeflater = new Deflater();

	private static final Inflater sharedInflater = new Inflater();

	private static final Text TIP = Text.valueOf("tip");

	public static byte[] compress(byte[] data) {
		return compress(data, 0, data.length);
	}

	public static byte[] compress(final byte[] data, final int off,
			final int len) {
		if (len < 44) {
			if (len > 0 && data[0] != 0) {
				final byte[] grow = new byte[len + 1];
				grow[0] = 'u';
				System.arraycopy(data, off, grow, 1, len);
				return grow;
			} else {
				return data;
			}
		}
		final byte[] buffer = new byte[len + 1];
		synchronized (Store.class) {
			final Deflater d = sharedDeflater;
			d.reset();
			d.setInput(data, off, len);
			d.finish();
			final int n = d.deflate(buffer);
			if (d.finished() && n < buffer.length) {
				final byte[] shrink = new byte[n];
				System.arraycopy(buffer, 0, shrink, 0, n);
				return shrink;
			}
		}
		if (len > 0 && data[0] != 0) {
			buffer[0] = 'u';
			System.arraycopy(data, off, buffer, 1, len);
			return buffer;
		} else {
			return data;
		}
	}

	public static byte[] decompress(final byte[] data)
			throws DataFormatException {
		return decompress(data, 0, data.length);
	}

	public static byte[] decompress(final byte[] data, final int off,
			final int len) throws DataFormatException {
		if (len > 0) {
			int b = data[0];
			if (b == 0x78) {
				final ArrayList<byte[]> chain = new ArrayList<byte[]>();
				byte[] buffer = new byte[len * 2];
				int fill = 0;
				int size = 0;
				synchronized (Store.class) {
					Inflater i = sharedInflater;
					i.reset();
					i.setInput(data, off, len);
					while (!i.finished()) {
						if (i.needsInput()) {
							throw new DataFormatException(
									"compressed data truncated");
						} else {
							int n = i.inflate(buffer, fill, buffer.length
									- fill);
							size += n;
							if ((fill += n) == buffer.length) {
								chain.add(buffer);
								buffer = new byte[buffer.length];
								fill = 0;
							}
						}
					}
				}
				final byte[] grow = new byte[size];
				fill = 0;
				for (byte[] chunk : chain) {
					int n = chunk.length;
					System.arraycopy(chunk, 0, grow, fill, n);
					fill += n;
				}
				if (fill < size) {
					System.arraycopy(buffer, 0, grow, fill, size - fill);
				}
				return grow;
			} else if (b == 'u') {
				final byte[] shrink = new byte[len - 1];
				System.arraycopy(data, off + 1, shrink, 0, len - 1);
				return shrink;
			} else if (b != 0) {
				throw new DataFormatException("unknown compression type" + b);
			}
		}
		return data;
	}

	private static File findStore(String cwd) {
		File scan = new File(cwd);
		try {
			while (scan != null) {
				File store = new File(scan, ".hg");
				if (store.isDirectory()) {
					return store;
				} else {
					File next = scan.getParentFile();
					if (next.equals(scan)) {
						// in case getParentFile returns the same
						break;
					} else {
						scan = next;
					}
				}
			}
		} catch (NullPointerException e) {
			// in case getParentFile returns null
		}
		throw new IllegalArgumentException("no store found for '" + cwd + "'");
	}

	private final File base;

	private final History history;

	private final Manifest manifest;

	private HashMap<Text, Version> tags;

	public Store() throws IOException {
		this(System.getProperty("user.dir", "."));
	}

	public Store(File base) throws IOException {
		this.base = base.getAbsoluteFile();
		this.history = new History(base);
		this.manifest = new Manifest(base);
	}

	public Store(String cwd) throws IOException {
		this(findStore(cwd));
	}

	public Element file(Text path) throws IOException {
		return new Element(base, path);
	}

	public Version lookup(Text key) throws IOException, DataFormatException {
		Version result = tags().get(key);
		if (result == null) {
			result = history.lookup(key);
		}
		return result;
	}

	public final Manifest manifest() {
		return manifest;
	}

	public synchronized HashMap<Text, Version> tags() throws IOException,
			DataFormatException {
		if (tags == null) {
			tags = new HashMap<Text, Version>();
			final Element f = file(HG_TAGS);
			final List<Version> h = f.heads();
			Collections.reverse(h);
			for (Version v : h) {
				addTags(Text.constant(f.read(v)));
			}
			try {
				File file = new File(base, "localtags");
				if (file.isFile()) {
					FileInputStream in = new FileInputStream(file);
					try {
						addTags(new TextBuffer().append(in).toText());
					} finally {
						in.close();
					}
				}
			} catch (FileNotFoundException optional) {
				// ignored
			}
			tags.put(TIP, history.tip());
		}
		return tags;
	}

	public List<Text> tags(Version v) throws IOException, DataFormatException {
		List<Text> result = new ArrayList<Text>();
		for (Entry<Text, Version> e : tags().entrySet()) {
			if (v.equals(e.getValue())) {
				result.add(e.getKey());
			}
		}
		return result;
	}

	public List<TagEntry> taglist() throws IOException, DataFormatException {
		ArrayList<TagEntry> result = new ArrayList<TagEntry>();
		for (Map.Entry<Text, Version> e : tags().entrySet()) {
			int g = -2;
			try {
				g = history.generation(e.getValue());
			} catch (Exception unknown) {
				// ignored
			}
			result.add(new TagEntry(g, e.getKey(), e.getValue()));
		}
		Collections.sort(result);
		return result;
	}

	public Undo transaction() throws IOException {
		TextBuffer b = new TextBuffer();
		try {
			FileInputStream in = new FileInputStream(new File(base, "dirstate"));
			try {
				b.append(in);
			} finally {
				in.close();
			}
		} catch (FileNotFoundException e) {
			// not yet created
		}
		FileOutputStream out = new FileOutputStream(new File(base,
				"journal.dirstate"));
		try {
			b.writeTo(out);
		} finally {
			out.close();
		}
		final File journal = new File(base, "journal");
		Runnable after = new Runnable() {

			public void run() {
				forceRename(journal, new File(base, "undo"));
				forceRename(new File(base, "journal.dirstate"), new File(base,
						"undo.dirstate"));
			}

		};
		return new Undo(journal, after);
	}

	private void addTags(Text file) {
		TextParser tp = new TextParser(file, NL);
		while (tp.hasNext()) {
			addTag(tp.next());
		}
	}

	private void addTag(Text text) {
		int mark = text.lastIndexOf(' ');
		if (mark != -1) {
			Text n = trim(text, 0, mark);
			Text k = text.part(mark + 1, text.size() - mark - 1);
			tags.put(k, Version.create(n));
		}
	}

	private Text trim(Text text, int off, int len) {
		while (len > 0 && text.getByte(off + len - 1) <= ' ') {
			len -= 1;
		}
		while (len > 0 && text.getByte(off) <= ' ') {
			len -= 1;
			off += 1;
		}
		return text.part(off, len);
	}

	private static void forceRename(File from, File to) {
		if (!from.renameTo(to)) {
			to.delete();
			from.renameTo(to);
		}
	}
}
