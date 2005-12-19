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
import java.util.Iterator;

import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Text button HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiButton extends HuiLabel {

    private static final Text HTML_BUTTON1, HTML_BUTTON2, HTML_BUTTON3,
            HTML_BUTTON4, HTML_BUTTON5;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiButton.class);
        HTML_BUTTON1 = list.next();
        HTML_BUTTON2 = list.next();
        HTML_BUTTON3 = list.next();
        HTML_BUTTON4 = list.next();
        HTML_BUTTON5 = list.next();
    }

    private Text action;

    public Text getAction() {
        return action;
    }

    @Override
    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        if (handler != null) {
            handler.action(getAction(), path);
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_BUTTON1.writeTo(out);
        getId().writeTo(out);
        HTML_BUTTON2.writeTo(out);
        getId().writeTo(out);
        if (getAction() != null) {
            HTML_BUTTON3.writeTo(out);
            getAction().writeTo(out);
        }
        if (!isEnabled()) {
            HTML_DISABLED.writeTo(out);
        }
        HTML_BUTTON4.writeTo(out);
        renderText(out, getText());
        HTML_BUTTON5.writeTo(out);
    }

    public void setAction(Text action) {
        this.action = action;
    }

}
