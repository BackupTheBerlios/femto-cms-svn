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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * File element implementation.
 * 
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class Element extends History {

    private static final String _HG_ = ".hg" + StreamFactory.Local.SEPARATOR;

    private static final String _HG_HG_ = ".hg.hg"
            + StreamFactory.Local.SEPARATOR;

    private static final String _I_ = ".i" + StreamFactory.Local.SEPARATOR;

    private static final String _I_HG_ = ".i.hg"
            + StreamFactory.Local.SEPARATOR;

    private static final String _D_ = ".d" + StreamFactory.Local.SEPARATOR;

    private static final String _D_HG_ = ".d.hg"
            + StreamFactory.Local.SEPARATOR;

    private static final String M_COPY = "copy";

    private static final String M_COPYREV = "copyrev";

    private static final String M_COLON = ": ";

    private static final byte[] _MM = { 1, '\n' };

    private static String encode(String path) {
        path = replace(path, _HG_, _HG_HG_);
        path = replace(path, _I_, _I_HG_);
        path = replace(path, _D_, _D_HG_);
        return path;
    }

    private static String replace(String t, String a, String b) {
        int start = 0, mark;
        StringBuffer buffer = null;
        while ((mark = t.indexOf(a, start)) != -1) {
            if (buffer == null) {
                buffer = new StringBuffer(t.length() + b.length());
            }
            buffer.append(t.substring(start, mark)).append(b);
            start = mark + a.length();
        }
        return buffer == null ? t : buffer.toString();
    }

    private Element(StreamFactory data, String prefix, String baseName)
            throws IOException {
        super(data, data.join(prefix, baseName + ".i"), data.join(prefix,
                baseName + ".d"));
    }

    public Element(StreamFactory base, String path) throws IOException {
        this(base, "data", encode(path));
    }

    public Map<String, String> meta(Version version) throws IOException {
        final byte[] contents = contents(version);
        if (!Store.startsWith(contents, _MM)) {
            return Collections.emptyMap();
        }
        HashMap<String, String> result = new HashMap<String, String>();
        StringTokenizer tok = new StringTokenizer(Store.toString(contents, 2,
                Store.indexOf(contents, _MM, 2) - 2), "\n");
        while (tok.hasMoreTokens()) {
            final String line = tok.nextToken();
            final int mark = line.indexOf(M_COLON);
            result.put(line.substring(0, mark), line.substring(mark
                    + M_COLON.length()));
        }
        return result;
    }

    public byte[] read(Version version) throws IOException {
        final byte[] result = contents(version);
        if (!Store.startsWith(result, _MM)) {
            return result;
        }
        // strip meta-data
        int mark = Store.indexOf(result, _MM, 2) + 2;
        final byte[] stripped = new byte[result.length - mark];
        System.arraycopy(result, mark, stripped, 0, stripped.length);
        return stripped;
    }

    public String[] renamed(Version version) throws IOException {
        Map<String, String> meta = meta(version);
        if (meta.containsKey(M_COPY)) {
            return new String[] { meta.get(M_COPY), meta.get(M_COPYREV) };
        } else {
            return null;
        }
    }
}
