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

import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Text input HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiText extends HuiNode {

    private static final Text HTML_AREA1, HTML_AREA2, HTML_AREA3, HTML_AREA4,
            HTML_AREA5, HTML_AREA6;

    private static final Text HTML_INPUT1, HTML_INPUT2, HTML_INPUT31,
            HTML_INPUT32, HTML_INPUT4, HTML_INPUT5;

    private static final Text HTML_READONLY;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiText.class);
        HTML_INPUT1 = list.next();
        HTML_INPUT2 = list.next();
        HTML_INPUT31 = list.next();
        HTML_INPUT32 = list.next();
        HTML_INPUT4 = list.next();
        HTML_INPUT5 = list.next();
        HTML_AREA1 = list.next();
        HTML_AREA2 = list.next();
        HTML_AREA3 = list.next();
        HTML_AREA4 = list.next();
        HTML_AREA5 = list.next();
        HTML_AREA6 = list.next();
        HTML_READONLY = list.next();
    }

    private int cols;

    private boolean editable = true;

    private int rows = 1;

    private boolean secret = false;

    private Text value;

    public HuiText() {
    }

    public HuiText(HuiText old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
        this.cols = old.cols;
        this.editable = old.editable;
        this.rows = old.rows;
        this.secret = old.secret;
        this.value = old.value;
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

    public boolean isEditable() {
        return editable;
    }

    public boolean isSecret() {
        return secret;
    }

    @Override
    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        if (isEditable()) {
            setValue(value);
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        if (rows <= 1) {
            HTML_INPUT1.writeTo(out);
            getId().writeTo(out);
            HTML_INPUT2.writeTo(out);
            getId().writeTo(out);
            (secret ? HTML_INPUT32 : HTML_INPUT31).writeTo(out);
            renderValue(out);
            if (getCols() > 0) {
                HTML_INPUT4.writeTo(out);
                Text.valueOf(getCols(), 10).writeTo(out);
            }
            if (!isEditable()) {
                HTML_READONLY.writeTo(out);
            }
            if (!isEnabled()) {
                HTML_DISABLED.writeTo(out);
            }
            HTML_INPUT5.writeTo(out);
        } else {
            HTML_AREA1.writeTo(out);
            getId().writeTo(out);
            HTML_AREA2.writeTo(out);
            getId().writeTo(out);
            HTML_AREA3.writeTo(out);
            Text.valueOf(getRows(), 10).writeTo(out);
            HTML_AREA4.writeTo(out);
            Text.valueOf(getCols(), 10).writeTo(out);
            if (!isEditable()) {
                HTML_READONLY.writeTo(out);
            }
            if (!isEnabled()) {
                HTML_DISABLED.writeTo(out);
            }
            HTML_AREA5.writeTo(out);
            renderValue(out);
            HTML_AREA6.writeTo(out);
        }
    }

    protected void renderValue(OutputStream out) throws IOException {
        renderText(out, getValue());
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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
    protected void loadState(InputStream in) throws IOException {
        // text ist too big to fit into state
    }

    @Override
    public void saveState(OutputStream out) throws IOException {
        // text ist too big to fit into state
    }

}
