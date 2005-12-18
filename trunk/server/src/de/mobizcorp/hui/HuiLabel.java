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

/**
 * Text label HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiLabel extends HuiNode {

    private static final Text HTML_SPAN_CLOSE = Text.constant((byte) '<',
            (byte) '/', (byte) 's', (byte) 'p', (byte) 'a', (byte) 'n',
            (byte) '>', (byte) '\n');

    private static final Text HTML_SPAN_OPEN1 = Text.constant((byte) '<',
            (byte) 's', (byte) 'p', (byte) 'a', (byte) 'n', (byte) ' ',
            (byte) 'i', (byte) 'd', (byte) '=', (byte) '"');

    private static final Text HTML_SPAN_OPEN2 = Text.constant((byte) '"',
            (byte) '>');

    private Text text;

    @Override
    public void appendState(OutputStream out) throws IOException {
        // label has no state
    }

    public Text getText() {
        return text;
    }

    @Override
    public void loadState(InputStream in) throws IOException {
        // label has no state
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_SPAN_OPEN1.writeTo(out);
        getId().writeTo(out);
        HTML_SPAN_OPEN2.writeTo(out);
        renderText(out, getText());
        HTML_SPAN_CLOSE.writeTo(out);
    }

    public void setText(Text text) {
        this.text = text;
    }

}
