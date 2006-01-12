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
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import de.mobizcorp.lib.Text;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Text label HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiLabel extends HuiNode {

    private static final Text HTML_SPAN_CLOSE, HTML_SPAN_OPEN1,
            HTML_SPAN_OPEN2, HTML_LINK_OPEN, HTML_LINK_CLOSE;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiLabel.class);
        HTML_SPAN_CLOSE = list.next();
        HTML_SPAN_OPEN1 = list.next();
        HTML_SPAN_OPEN2 = list.next();
        HTML_LINK_OPEN = list.next();
        HTML_LINK_CLOSE = list.next();
    }

    private Text action;

    private Text text;

    public HuiLabel() {
    }

    public HuiLabel(HuiLabel old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
        this.action = old.action;
        this.text = old.text;
    }

    public Text getAction() {
        return action;
    }

    public Text getText() {
        return text;
    }

    @Override
    protected void loadState(InputStream in) throws IOException {
        // label has no state
    }

    @Override
    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        if (handler != null) {
            handler.action(getAction(), path);
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        if (action != null) {
            HTML_LINK_OPEN.writeTo(out);
            getId().writeTo(out);
            out.write((byte) '=');
            renderText(out, getAction());
            HTML_SPAN_OPEN2.writeTo(out);
        }
        HTML_SPAN_OPEN1.writeTo(out);
        getId().writeTo(out);
        HTML_SPAN_OPEN2.writeTo(out);
        renderText(out, getText());
        HTML_SPAN_CLOSE.writeTo(out);
        if (action != null) {
            HTML_LINK_CLOSE.writeTo(out);
        }
    }

    @Override
    public void saveState(OutputStream out) throws IOException {
        // label has no state
    }

    public void setAction(Text action) {
        this.action = action;
    }

    public void setText(Text text) {
        this.text = text;
    }

}
