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
 * Container HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiPanel extends HuiNode {

    private static final Text HTML_ELT_CLOSE, HTML_ELT_OPEN1, HTML_ELT_OPEN2,
            HTML_ELT_OPEN3, HTML_ROW_CLOSE, HTML_ROW_OPEN, HTML_TAB_CLOSE,
            HTML_TAB_OPEN1, HTML_TAB_OPEN2;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiPanel.class);
        HTML_ELT_CLOSE = list.next();
        HTML_ELT_OPEN1 = list.next();
        HTML_ELT_OPEN2 = list.next();
        HTML_ELT_OPEN3 = list.next();
        HTML_ROW_CLOSE = list.next();
        HTML_ROW_OPEN = list.next();
        HTML_TAB_CLOSE = list.next();
        HTML_TAB_OPEN1 = list.next();
        HTML_TAB_OPEN2 = list.next();
    }

    private int cols;

    private Text title;

    public HuiPanel() {
    }

    public HuiPanel(HuiPanel old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
        this.cols = old.cols;
        this.title = old.title;
    }

    public int getCols() {
        return cols;
    }

    public Text getTitle() {
        return title;
    }

    @Override
    protected void loadState(InputStream in) throws IOException {
        HuiNode scan = getChild();
        while (scan != null) {
            scan.loadState(in);
            scan = scan.getSibling();
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_TAB_OPEN1.writeTo(out);
        getId().writeTo(out);
        HTML_TAB_OPEN2.writeTo(out);
        HuiNode scan = getChild();
        int c = getCols();
        while (scan != null) {
            if (c == getCols()) {
                HTML_ROW_OPEN.writeTo(out);
            }
            HTML_ELT_OPEN1.writeTo(out);
            Text.valueOf(scan.getColSpan(), 10).writeTo(out);
            HTML_ELT_OPEN2.writeTo(out);
            Text.valueOf(scan.getRowSpan(), 10).writeTo(out);
            HTML_ELT_OPEN3.writeTo(out);
            scan.renderNode(out);
            HTML_ELT_CLOSE.writeTo(out);
            if ((c -= scan.getColSpan()) <= 0) {
                HTML_ROW_CLOSE.writeTo(out);
                c = getCols();
            }
            scan = scan.getSibling();
        }
        HTML_TAB_CLOSE.writeTo(out);
    }

    @Override
    public void saveState(OutputStream out) throws IOException {
        HuiNode scan = getChild();
        while (scan != null) {
            scan.saveState(out);
            scan = scan.getSibling();
        }
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

}
