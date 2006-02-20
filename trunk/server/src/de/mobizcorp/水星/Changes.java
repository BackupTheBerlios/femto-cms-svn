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

import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Change log abstraction.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Changes extends History {

    public static class LogEntry {
        private static long parseTime(String text) {
            // we ignore the timezone field for now
            int mark = text.indexOf(' ');
            final double nixTime;
            if (mark != -1) {
                nixTime = Double.parseDouble(text.substring(0, mark));
            } else {
                nixTime = Double.parseDouble(text);
            }
            return (long) (nixTime * 1000);
        }

        public final Version changeset;

        public final String[] files;

        public final int generation;

        public final Version manifest;

        public final String message;

        public final long time;

        public final String user;

        public LogEntry(Version changeset, int generation, final byte[] data) {
            this.changeset = changeset;
            this.generation = generation;
            final int mark = Store.indexOf(data, _N_N) + 2;
            final StringTokenizer tok = new StringTokenizer(Store.toString(data, 0, mark), "\n");
            this.manifest = Version.create(tok.nextToken());
            this.user = tok.nextToken();
            this.time = parseTime(tok.nextToken());
            final ArrayList<String> buffer = new ArrayList<String>();
            while (tok.hasMoreTokens()) {
                buffer.add(tok.nextToken());
            }
            this.files = buffer.toArray(new String[buffer.size()]);
            this.message = Store.toString(data, mark, data.length - mark);
        }

        public void writeTo(Writer writer) throws IOException {
            writer.write("changeset:   ");
            writer.write(Integer.toString(generation, 10));
            writer.write(':');
            writer.write(changeset.toString());
            writer.write("\nuser:        ");
            writer.write(user);
            writer.write("\ndate:        ");
            writer.write(new Timestamp(time).toString());
            writer.write("\nfiles:       ");
            for (int i = 0; i < files.length; i++) {
                if (i > 0) {
                    writer.write(' ');
                }
                writer.write(files[i]);
            }
            writer.write("\ndescription:\n");
            writer.write(message);
            writer.write('\n');
        }
    }
    
    private static final byte[] _N_N = { '\n', '\n'};

    private static final String CHANGE_CHUNKS = "00changelog.d";

    private static final String CHANGE_INDEX = "00changelog.i";

    public Changes(final StreamFactory base) throws IOException {
        super(base, CHANGE_INDEX, CHANGE_CHUNKS);
    }

    public LogEntry read(Version version) throws IOException {
        return new LogEntry(version, generation(version), contents(version));
    }
}
