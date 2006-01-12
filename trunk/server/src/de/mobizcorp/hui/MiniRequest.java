/*
 * Half User Interface.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
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
package de.mobizcorp.hui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;

/**
 * HTTP request parser and model.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class MiniRequest {

    private final HashMap<Text, Text> headers = new HashMap<Text, Text>();

    private Text method;

    private Text url;

    private Text version;

    public static MiniRequest from(final InputStream in) throws IOException {
        MiniRequest result = new MiniRequest();
        TextBuffer buffer = new TextBuffer();
        while (crlf(in)) {
            // ignore initial CRLF
        }
        if (token(in, buffer)) {
            result.setMethod(buffer.toText());
        } else {
            return null;
        }
        if (word(in, buffer)) {
            result.setUrl(buffer.toText());
        } else {
            return null;
        }
        if (word(in, buffer)) {
            result.setVersion(buffer.toText());
        } else {
            return null;
        }
        if (!crlf(in)) {
            return null;
        }
        while (header(in, result, buffer)) {
            if (!crlf(in)) {
                break;
            } else if (crlf(in)) {
                break; // end of headers
            }
        }
        return result;
    }

    private static boolean header(InputStream in, MiniRequest result,
            TextBuffer buffer) throws IOException {
        if (!token(in, buffer)) {
            return false;
        }
        Text name = buffer.toText();
        if (in.read() != ':') {
            // skip garbage
            int b;
            do {
                in.mark(1);
                b = in.read();
                if (b < ' ') {
                    break;
                }
            } while (b != -1);
            in.reset();
            return false;
        }
        if (!lws(in)) {
            return false;
        }
        buffer.clear();
        for (;;) {
            int b;
            do {
                in.mark(1);
                b = in.read();
                if (b > ' ') {
                    buffer.append((byte) b);
                } else {
                    break;
                }
            } while (b != -1);
            in.reset();
            if (lws(in)) {
                buffer.append((byte) ' ');
            } else {
                break;
            }
        }
        result.setHeader(name, buffer.toText());
        return true;
    }

    private static boolean token(final InputStream in, final TextBuffer buffer)
            throws IOException {
        buffer.clear();
        int b;
        for (;;) {
            in.mark(1);
            b = in.read();
            if (b > ' ' && !sep(b)) {
                buffer.append((byte) b);
            } else {
                break;
            }
        }
        if (sp(b)) {
            do {
                in.mark(1);
                b = in.read();
            } while (sp(b));
        }
        in.reset();
        return buffer.size() > 0;
    }

    private static boolean word(final InputStream in, final TextBuffer buffer)
            throws IOException {
        buffer.clear();
        int b;
        for (;;) {
            in.mark(1);
            b = in.read();
            if (b > ' ') {
                buffer.append((byte) b);
            } else {
                break;
            }
        }
        if (sp(b)) {
            do {
                in.mark(1);
                b = in.read();
            } while (sp(b));
        }
        in.reset();
        return buffer.size() > 0;
    }

    private static boolean crlf(final InputStream in) throws IOException {
        in.mark(2);
        int b = in.read();
        if (b == '\r') {
            b = in.read();
        }
        if (b != '\n') {
            in.reset();
            return false;
        }
        return true;
    }

    private static boolean lws(final InputStream in) throws IOException {
        in.mark(3);
        int b = in.read();
        if (!sp(b)) {
            if (b == '\r') {
                b = in.read();
            }
            if (b != '\n') {
                in.reset();
                return false;
            } else {
                b = in.read();
            }
            if (!sp(b)) {
                in.reset();
                return false;
            }
        }
        do {
            in.mark(1);
            b = in.read();
        } while (sp(b));
        in.reset();
        return true;
    }

    private static boolean sp(final int b) {
        return b == ' ' || b == '\t';
    }

    private static boolean sep(final int b) {
        return b == ' ' || b == '\t' || b == '(' || b == ')' || b == '<'
                || b == '>' || b == '@' || b == ',' || b == ';' || b == ':'
                || b == '\\' || b == '"' || b == '/' || b == '[' || b == ']'
                || b == '?' || b == '=' || b == '{' || b == '}';
    }

    public Text getHeader(Text name) {
        return headers.get(name);
    }

    public void setHeader(Text name, Text value) {
        headers.put(name, value);
    }

    public Text getMethod() {
        return method;
    }

    public void setMethod(Text method) {
        this.method = method;
    }

    public Text getUrl() {
        return url;
    }

    public void setUrl(Text url) {
        this.url = url;
    }

    public Text getVersion() {
        return version;
    }

    public void setVersion(Text version) {
        this.version = version;
    }
}
