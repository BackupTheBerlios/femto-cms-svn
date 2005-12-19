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
import java.util.Iterator;

import de.mobizcorp.qu8ax.Resolver;
import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Base class for all HUI elements.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public abstract class HuiNode {

    public static final Text HTML_DISABLED;

    private static final Text HTML_FORM1, HTML_FORM2, HTML_FORM3;

    private static final Text HTML_PAGE1, HTML_PAGE2, HTML_PAGE3;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiNode.class);
        HTML_FORM1 = list.next();
        HTML_FORM2 = list.next();
        HTML_FORM3 = list.next();
        HTML_PAGE1 = list.next();
        HTML_PAGE2 = list.next();
        HTML_PAGE3 = list.next();
        HTML_DISABLED = list.next();
    }

    public static void renderText(final OutputStream out, final Text text)
            throws IOException {
        if (text == null) {
            return;
        }
        final int end = text.size();
        for (int i = 0; i < end; i++) {
            byte b = text.getByte(i);
            if (b == 34) {
                Resolver.ENT_QUOT.writeTo(out);
            } else if (b == 38) {
                Resolver.ENT_AMP.writeTo(out);
            } else if (b == 60) {
                Resolver.ENT_LT.writeTo(out);
            } else if (b == 62) {
                Resolver.ENT_GT.writeTo(out);
            } else {
                out.write(b);
            }
        }
    }

    private HuiNode child;

    private int colSpan = 1;

    private boolean enabled = true;

    private Text id;

    private int rowSpan = 1;

    private HuiNode sibling;

    public void addChild(final HuiNode child) {
        if (this == child) {
            throw new IllegalArgumentException("loop");
        }
        HuiNode scan = this.child;
        if (scan == null) {
            this.child = child;
        } else {
            while (scan.sibling != null) {
                scan = scan.sibling;
            }
            scan.sibling = child;
        }
        child.sibling = null; // prevent loops
    }

    public int childCount() {
        HuiNode scan = child;
        int n = 0;
        while (scan != null) {
            n += 1;
            scan = scan.sibling;
        }
        return n;
    }

    public HuiNode find(final Text id) {
        if (id.equals(getId())) {
            return this;
        }
        HuiNode scan = child;
        while (scan != null) {
            HuiNode found = scan.find(id);
            if (found != null) {
                return found;
            }
            scan = scan.sibling;
        }
        return null;
    }

    public HuiNode getChild() {
        return child;
    }

    public int getColSpan() {
        return colSpan;
    }

    public Text getId() {
        return id;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public HuiNode getSibling() {
        return sibling;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public abstract void loadState(InputStream in) throws IOException;

    public Path<HuiNode> path(final Text id) {
        if (id.equals(getId())) {
            return new Path<HuiNode>(this, null);
        }
        HuiNode scan = child;
        while (scan != null) {
            Path<HuiNode> found = scan.path(id);
            if (found != null) {
                return new Path<HuiNode>(this, found);
            }
            scan = scan.sibling;
        }
        return null;
    }

    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        // no effect by default
    }

    public void removeAll() {
        HuiNode scan = child;
        child = null;
        while (scan != null) {
            HuiNode next = scan.sibling;
            scan.sibling = null;
            scan = next;
        }
    }

    public void removeChild(HuiNode child) {
        HuiNode scan = this.child;
        if (scan == child) {
            this.child = child.sibling;
            child.sibling = null;
            return;
        }
        while (scan != null) {
            if (scan.sibling == child) {
                scan.sibling = child.sibling;
                child.sibling = null;
                break;
            }
            scan = scan.sibling;
        }
    }

    public abstract void renderNode(OutputStream out) throws IOException;

    public void renderPage(OutputStream out) throws IOException {
        HTML_PAGE1.writeTo(out);
        if (this instanceof HuiPanel) {
            final Text title = ((HuiPanel) this).getTitle();
            if (title != null) {
                title.writeTo(out);
            }
        }
        HTML_PAGE2.writeTo(out);
        renderTree(out);
        HTML_PAGE3.writeTo(out);
    }

    public void renderTree(OutputStream out) throws IOException {
        HTML_FORM1.writeTo(out);
        StateCodec codec = new StateCodec(out);
        saveState(codec);
        codec.flush();
        HTML_FORM2.writeTo(out);
        renderNode(out);
        HTML_FORM3.writeTo(out);
    }

    public abstract void saveState(OutputStream out) throws IOException;

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(Text id) {
        this.id = id;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public void writeState(OutputStream out) throws IOException {
        out.write('/');
        StateCodec codec = new StateCodec(out);
        saveState(codec);
        codec.flush();
    }

}
