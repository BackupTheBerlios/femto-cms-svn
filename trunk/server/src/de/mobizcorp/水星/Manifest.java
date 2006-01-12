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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextParser;

/**
 * Manifest file implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Manifest extends History {
	public static final class Entry {
		public final boolean execute;

		public final Version version;

		public Entry(Version version, boolean execute) {
			this.version = version;
			this.execute = execute;
		}
	}

	private static final Text X = Text.constant((byte) 'x');

	public static void main(String args[]) throws Exception {
		Manifest manifest = new Store(args[0]).manifest();
		HashMap<Text, Entry> map = manifest.read(manifest.tip());
		ArrayList<Text> files = new ArrayList<Text>();
		files.addAll(map.keySet());
		for (Text f : files) {
			Entry entry = map.get(f);
			System.out.print(entry.version);
			System.out.print(entry.execute ? " 755 " : " 644 ");
			System.out.println(f);
		}
	}

	private HashMap<Text, Entry> currentManifest;

	private Version currentVersion;

	public Manifest(File base) throws IOException {
		super(new File(base, "00manifest.i"), new File(base, "00manifest.d"));
	}

	public synchronized HashMap<Text, Entry> read(Version version)
			throws IOException, DataFormatException {
		if (!version.equals(currentVersion)) {
			currentVersion = version;
			currentManifest = new HashMap<Text, Entry>();
			final TextParser tp = new TextParser(Text
					.constant(contents(version)), Store.NL);
			while (tp.hasNext()) {
				Text line = tp.next();
				int j = line.lastIndexOf(0);
				if (j != -1) {
					Text f = line.part(0, j);
					Version v = Version.create(line.part(j + 1, 40));
					boolean x = X.equals(line
							.part(j + 41, line.size() - j - 41));
					currentManifest.put(f, new Entry(v, x));
				}
			}
		}
		return currentManifest;
	}
}
