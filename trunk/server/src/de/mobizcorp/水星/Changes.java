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

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextParser;

/**
 * Change log abstraction.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Changes extends History {

    public static class LogEntry {
        private static long parseTime(Text text) {
            // we ignore the timezone field for now
            int mark = text.indexOf(' ');
            final double nixTime;
            if (mark != -1) {
                nixTime = Double.parseDouble(text.part(0, mark).toString());
            } else {
                nixTime = Double.parseDouble(text.toString());
            }
            return (long) (nixTime * 1000);
        }

        public final Text[] files;

        public final Version manifest;

        public final Text message;

        public final long time;

        public final Text user;

        public LogEntry(final byte[] data) {
            final Text text = Text.constant(data);
            final int mark = text.indexOf(_N_N) + 2;
            final TextParser tp = new TextParser(text.part(0, mark), Store.NL);
            this.manifest = Version.create(tp.next());
            this.user = tp.next();
            this.time = parseTime(tp.next());
            final ArrayList<Text> buffer = new ArrayList<Text>();
            while (tp.hasNext()) {
                buffer.add(tp.next());
            }
            this.files = buffer.toArray(new Text[buffer.size()]);
            this.message = text.part(mark, text.size() - mark);
        }
    }

    private static final Text _N_N = Text.constant((byte) '\n', (byte) '\n');

    public Changes(File base) throws IOException {
        super(new File(base, "00changelog.i"), new File(base, "00changelog.d"));
    }

    public LogEntry read(Version version) throws IOException {
        return new LogEntry(contents(version));
    }
}
