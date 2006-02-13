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
import java.io.OutputStream;
import java.sql.Timestamp;
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

        public final Version changeset;

        public final Text[] files;

        public final Version manifest;

        public final Text message;

        public final long time;

        public final Text user;

        public final int generation;

        public LogEntry(Version changeset, int generation, final byte[] data) {
            this.changeset = changeset;
            this.generation = generation;
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
        
        public void writeTo(OutputStream out) throws IOException {
            Text.valueOf("changeset:   ").writeTo(out);
            Text.valueOf(generation, 10).writeTo(out);
            out.write(':');
            changeset.toText().writeTo(out);
            Text.valueOf("\nuser:        ").writeTo(out);
            user.writeTo(out);
            Text.valueOf("\ndate:        ").writeTo(out);
            Text.valueOf(new Timestamp(time).toString()).writeTo(out);
            Text.valueOf("\nfiles:       ").writeTo(out);
            for (int i = 0; i < files.length; i++) {
                if (i > 0) {
                    out.write(' ');
                }
                files[i].writeTo(out);
            }
            Text.valueOf("\ndescription:\n").writeTo(out);
            message.writeTo(out);
            out.write('\n');
        }
    }

    private static final Text _N_N = Text.constant((byte) '\n', (byte) '\n');

    public Changes(File base) throws IOException {
        super(new File(base, "00changelog.i"), new File(base, "00changelog.d"));
    }

    public LogEntry read(Version version) throws IOException {
        return new LogEntry(version, generation(version), contents(version));
    }
}
