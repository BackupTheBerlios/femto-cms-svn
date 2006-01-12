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
 * Tabbed pane HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiTabs extends HuiNode {

    private static final Text HTML_TAB1, HTML_TAB21, HTML_TAB22, HTML_TAB3,
            HTML_TABS1, HTML_TABS2, HTML_TABS3;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiTabs.class);
        HTML_TAB1 = list.next();
        HTML_TAB21 = list.next();
        HTML_TAB22 = list.next();
        HTML_TAB3 = list.next();
        HTML_TABS1 = list.next();
        HTML_TABS2 = list.next();
        HTML_TABS3 = list.next();
    }

    private int selected;

    private transient HuiPanel selectedTab;

    public HuiTabs() {
    }

    public HuiTabs(HuiTabs old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
        this.selected = old.selected;
        this.selectedTab = null;
    }

    @Override
    public void addChild(final HuiNode child) {
        this.selectedTab = null;
        super.addChild(child);
    }

    public int getSelected() {
        return selected;
    }

    public HuiPanel getSelectedTab() {
        if (selectedTab == null) {
            int count = selected;
            HuiNode scan = getChild();
            while (scan != null && --count >= 0) {
                scan = scan.getSibling();
            }
            selectedTab = (HuiPanel) scan;
        }
        return selectedTab;
    }

    @Override
    protected void loadState(final InputStream in) throws IOException {
        setSelected(RFC2279.read(in));
        final HuiPanel tab = getSelectedTab();
        if (tab != null) {
            tab.loadState(in);
        }
    }

    @Override
    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        setSelected(value.toInt(Text.MAX_RADIX));
    }

    @Override
    public void renderNode(final OutputStream out) throws IOException {
        HTML_TABS1.writeTo(out);
        int count = 0;
        HuiNode scan = getChild();
        while (scan != null) {
            final Text title = ((HuiPanel) scan).getTitle();
            HTML_TAB1.writeTo(out);
            getId().writeTo(out);
            out.write((byte) '=');
            Text.valueOf(count, Text.MAX_RADIX).writeTo(out);
            (count == selected ? HTML_TAB22 : HTML_TAB21).writeTo(out);
            renderText(out, title);
            HTML_TAB3.writeTo(out);
            scan = scan.getSibling();
            count++;
        }
        HTML_TABS2.writeTo(out);
        final HuiPanel tab = getSelectedTab();
        if (tab != null) {
            tab.renderNode(out);
        }
        HTML_TABS3.writeTo(out);
    }

    @Override
    public void saveState(final OutputStream out) throws IOException {
        RFC2279.write(getSelected(), out);
        final HuiPanel tab = getSelectedTab();
        if (tab != null) {
            tab.saveState(out);
        }
    }

    public void setSelected(final int selected) {
        this.selectedTab = null;
        this.selected = selected;
    }
}
