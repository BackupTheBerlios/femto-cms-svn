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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.lib.TextParser;

/**
 * File element implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Element extends History {

	private static final Text _HG = Text.valueOf(".hg/"), _HG_HG = Text
			.valueOf(".hg.hg/"), _I = Text.valueOf(".i/"), _I_HG = Text
			.valueOf(".i.hg/"), _D = Text.valueOf(".d/"), _D_HG = Text
			.valueOf(".d.hg/"), _MM = Text.constant((byte) 1, (byte) '\n'),
			M_COLON = Text.constant((byte) ':', (byte) ' '), M_COPY = Text
					.valueOf("copy"), M_COPYREV = Text.valueOf("copyrev");

	private static Text encode(Text path) {
		path = replace(path, _HG, _HG_HG);
		path = replace(path, _I, _I_HG);
		path = replace(path, _D, _D_HG);
		return path;
	}

	private static Text replace(Text t, Text a, Text b) {
		int start = 0, mark;
		TextBuffer buffer = null;
		while ((mark = t.indexOf(a, start)) != -1) {
			if (buffer == null) {
				buffer = new TextBuffer(t.size() + b.size());
			}
			buffer.append(t.part(start, mark - start)).append(b);
			start = mark + a.size();
		}
		return buffer == null ? t : buffer.toText();
	}

	private Element(File data, String baseName) throws IOException {
		super(new File(data, baseName + ".i"), new File(data, baseName + ".d"));
	}

	public Element(File base, Text path) throws IOException {
		this(new File(base, "data"), encode(path).toString());
	}

	public Map<Text, Text> meta(Version version) throws IOException,
			DataFormatException {
		Text text = Text.constant(contents(version));
		if (!text.startsWith(_MM)) {
			return Collections.emptyMap();
		}
		HashMap<Text, Text> result = new HashMap<Text, Text>();
		TextParser tp = new TextParser(text.part(2, text.indexOf(_MM, 2) - 2),
				Store.NL);
		while (tp.hasNext()) {
			final Text line = tp.next();
			final int mark = line.indexOf(M_COLON);
			result.put(line.part(0, mark), line.part(mark + M_COLON.size(),
					line.size() - mark - M_COLON.size()));
		}
		return result;
	}

	public byte[] read(Version version) throws IOException, DataFormatException {
		byte[] result = contents(version);
		if (result.length < 2 || result[0] != 1 || result[1] != '\n') {
			return result;
		}
        // strip meta-data
		Text text = Text.constant(result);
		int mark = text.indexOf(_MM, 2);
		return text.part(mark + 2, text.size() - mark - 2).toBytes();
	}

	public Text[] renamed(Version version) throws IOException,
			DataFormatException {
		Map<Text, Text> meta = meta(version);
		if (meta.containsKey(M_COPY)) {
			return new Text[] { meta.get(M_COPY), meta.get(M_COPYREV) };
		} else {
			return null;
		}
	}
}
