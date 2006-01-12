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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;

/**
 * Undo encapsulation with journal.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Undo {

	public static class Entry {
		private final File file;

		private final long position;

		public Entry(final File file, final long position) {
			this.file = file;
			this.position = position;
		}
	}

	private final File journal;

	private final Runnable after;

	private final FileOutputStream undoLog;

	private final HashMap<String, Entry> map = new HashMap<String, Entry>();

	public Undo(final File journal, final Runnable after) throws IOException {
		if (journal.exists()) {
			throw new IOException("need recovery: journal exists: " + journal);
		}
		this.journal = journal;
		this.after = after;
		this.undoLog = new FileOutputStream(journal);
	}

	public void add(File file, int offset) throws IOException {
		String path = file.getAbsolutePath();
		if (!map.containsKey(path)) {
			map.put(path, new Entry(file, offset));
			TextBuffer b = new TextBuffer();
			b.append(path).append((byte) 0).append(Text.valueOf(offset, 10))
					.append("\n");
			b.writeTo(undoLog);
			undoLog.flush();
		}
	}

	public void close() throws IOException {
		undoLog.close();
		journal.delete();
		if (after != null) {
			after.run();
		}
	}

	public void abort() throws FileNotFoundException {
		for (Entry e : map.values()) {
			try {
				RandomAccessFile ras = new RandomAccessFile(e.file, "w");
				try {
					ras.setLength(e.position);
				} finally {
					ras.close();
				}
			} catch (IOException x) {
				// skip
				x.printStackTrace();
			}
		}
		map.clear();
	}
}
