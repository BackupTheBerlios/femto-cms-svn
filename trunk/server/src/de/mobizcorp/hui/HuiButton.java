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
import java.io.OutputStream;

import de.mobizcorp.qu8ax.Text;

/**
 * Text button HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiButton extends HuiLabel {

    private static final Text HTML_INPUT1 = Text.constant((byte) '<',
            (byte) 'i', (byte) 'n', (byte) 'p', (byte) 'u', (byte) 't',
            (byte) ' ', (byte) 'i', (byte) 'd', (byte) '=', (byte) '"');

    private static final Text HTML_INPUT2 = Text.constant((byte) '"',
            (byte) ' ', (byte) 'n', (byte) 'a', (byte) 'm', (byte) 'e',
            (byte) '=', (byte) '"');

    private static final Text HTML_INPUT3 = Text.constant((byte) '"',
            (byte) ' ', (byte) 't', (byte) 'y', (byte) 'p', (byte) 'e',
            (byte) '=', (byte) '"', (byte) 's', (byte) 'u', (byte) 'b',
            (byte) 'm', (byte) 'i', (byte) 't', (byte) '"', (byte) ' ',
            (byte) 'v', (byte) 'a', (byte) 'l', (byte) 'u', (byte) 'e',
            (byte) '=', (byte) '"');

    private static final Text HTML_INPUT4 = Text.constant((byte) '"',
            (byte) '>', (byte) '\n');

    private Text action;

    public Text getAction() {
        return action;
    }

    @Override
    public void post(Text value, ActionHandler handler, HuiNode root) {
        if (handler != null) {
            handler.action(getAction(), this, root);
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_INPUT1.writeTo(out);
        getId().writeTo(out);
        HTML_INPUT2.writeTo(out);
        getId().writeTo(out);
        HTML_INPUT3.writeTo(out);
        renderText(out, getText());
        HTML_INPUT4.writeTo(out);
    }

    public void setAction(Text action) {
        this.action = action;
    }

}
