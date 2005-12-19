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
import java.util.ArrayList;
import java.util.Iterator;

import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Selection HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiSelect extends HuiNode {

    private static final Text HTML_SELECT1, HTML_SELECT2, HTML_SELECT3,
            HTML_SELECT4, HTML_OPTION1, HTML_OPTION21, HTML_OPTION22,
            HTML_OPTION3;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiSelect.class);
        HTML_SELECT1 = list.next();
        HTML_SELECT2 = list.next();
        HTML_SELECT3 = list.next();
        HTML_SELECT4 = list.next();
        HTML_OPTION1 = list.next();
        HTML_OPTION21 = list.next();
        HTML_OPTION22 = list.next();
        HTML_OPTION3 = list.next();
    }

    private final ArrayList<Text> options = new ArrayList<Text>();

    private int selected;

    public void addOption(Text value) {
        options.add(value);
    }

    public void clear() {
        options.clear();
    }

    public Text getOption(int index) {
        return options.get(index);
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public void loadState(InputStream in) throws IOException {
        setSelected(RFC2279.read(in));
    }

    @Override
    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        setSelected(value.toInt(Text.MAX_RADIX));
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_SELECT1.writeTo(out);
        getId().writeTo(out);
        HTML_SELECT2.writeTo(out);
        getId().writeTo(out);
        if (!isEnabled()) {
            HTML_DISABLED.writeTo(out);
        }
        HTML_SELECT3.writeTo(out);
        final int end = options.size();
        for (int i = 0; i < end; i++) {
            HTML_OPTION1.writeTo(out);
            Text.valueOf(i, Text.MAX_RADIX).writeTo(out);
            (i == selected ? HTML_OPTION22 : HTML_OPTION21).writeTo(out);
            renderText(out, options.get(i));
            HTML_OPTION3.writeTo(out);
        }
        HTML_SELECT4.writeTo(out);
    }

    @Override
    public void saveState(OutputStream out) throws IOException {
        RFC2279.write(getSelected(), out);
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

}
