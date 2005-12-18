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
import java.io.OutputStream;

import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextBuffer;

/**
 * Text input HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiText extends HuiNode {

    private static final Text HTML_INPUT1 = Text.constant((byte) '<',
            (byte) 'i', (byte) 'n', (byte) 'p', (byte) 'u', (byte) 't',
            (byte) ' ', (byte) 'i', (byte) 'd', (byte) '=', (byte) '"');

    private static final Text HTML_INPUT2 = Text.constant((byte) '"',
            (byte) ' ', (byte) 'n', (byte) 'a', (byte) 'm', (byte) 'e',
            (byte) '=', (byte) '"');

    private static final Text HTML_INPUT31 = Text.constant((byte) '"',
            (byte) ' ', (byte) 't', (byte) 'y', (byte) 'p', (byte) 'e',
            (byte) '=', (byte) '"', (byte) 't', (byte) 'e', (byte) 'x',
            (byte) 't', (byte) '"', (byte) ' ', (byte) 'v', (byte) 'a',
            (byte) 'l', (byte) 'u', (byte) 'e', (byte) '=', (byte) '"');

    private static final Text HTML_INPUT32 = Text.constant((byte) '"',
            (byte) ' ', (byte) 't', (byte) 'y', (byte) 'p', (byte) 'e',
            (byte) '=', (byte) '"', (byte) 'p', (byte) 'a', (byte) 's',
            (byte) 's', (byte) 'w', (byte) 'o', (byte) 'r', (byte) 'd',
            (byte) '"', (byte) ' ', (byte) 'v', (byte) 'a', (byte) 'l',
            (byte) 'u', (byte) 'e', (byte) '=', (byte) '"');

    private static final Text HTML_INPUT4 = Text.constant((byte) '"',
            (byte) ' ', (byte) 's', (byte) 'i', (byte) 'z', (byte) 'e',
            (byte) '=', (byte) '"');

    private static final Text HTML_INPUT5 = Text.constant((byte) '"',
            (byte) '>', (byte) '\n');

    private int cols;

    private int rows = 1;

    private boolean secret = false;

    private Text value;

    @Override
    public void appendState(OutputStream out) throws IOException {
        if (value != null) {
            value.writeTo(out);
        }
        out.write(0);
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public Text getValue() {
        return value;
    }

    public boolean isSecret() {
        return secret;
    }

    @Override
    public void loadState(InputStream in) throws IOException {
        TextBuffer buffer = new TextBuffer();
        int c;
        while ((c = in.read()) != -1) {
            if (c == 0) {
                break;
            } else {
                buffer.append((byte) c);
            }
        }
        setValue(buffer.toText());
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_INPUT1.writeTo(out);
        getId().writeTo(out);
        HTML_INPUT2.writeTo(out);
        getId().writeTo(out);
        (secret ? HTML_INPUT32 : HTML_INPUT31).writeTo(out);
        renderText(out, getValue());
        if (getCols() > 0) {
            HTML_INPUT4.writeTo(out);
            Text.valueOf(getCols(), 10).writeTo(out);
        }
        HTML_INPUT5.writeTo(out);
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public void setValue(Text value) {
        this.value = value;
    }

    @Override
    public void post(Text value, ActionHandler handler, HuiNode root) {
        setValue(value);
    }

}
