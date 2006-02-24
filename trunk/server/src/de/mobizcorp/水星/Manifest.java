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
import java.util.HashMap;
import java.util.StringTokenizer;

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

        @Override
        public String toString() {
            return version.toString() + (execute ? " 755 " : " 644 ");
        }
    }

    private static final String MANIFEST_CHUNKS = "00manifest.d";

    private static final String MANIFEST_INDEX = "00manifest.i";

    private static final String X = "x";

    private HashMap<String, Entry> currentManifest;

    private Version currentVersion;

    public Manifest(StreamFactory base) throws IOException {
        super(base, MANIFEST_INDEX, MANIFEST_CHUNKS);
    }

    public synchronized HashMap<String, Entry> read(Version version)
            throws IOException {
        if (!version.equals(currentVersion)) {
            currentVersion = version;
            currentManifest = new HashMap<String, Entry>();
            final StringTokenizer tok = new StringTokenizer(Util
                    .toString(contents(version)), "\n");
            while (tok.hasMoreTokens()) {
                String line = tok.nextToken();
                int j = line.lastIndexOf(0);
                if (j != -1) {
                    String f = line.substring(0, j);
                    Version v = Version.create(line.substring(j + 1, j + 41));
                    boolean x = X.equals(line.substring(j + 41));
                    currentManifest.put(f, new Entry(v, x));
                }
            }
        }
        return currentManifest;
    }
}
