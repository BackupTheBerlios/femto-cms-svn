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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.mobizcorp.qu8ax.Resolver;
import de.mobizcorp.qu8ax.Text;

/**
 * Base class for all HUI elements.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public abstract class HuiNode {

    private static final Text HTML_FORM1 = Text.constant((byte) '<',
            (byte) 'f', (byte) 'o', (byte) 'r', (byte) 'm', (byte) ' ',
            (byte) 'a', (byte) 'c', (byte) 't', (byte) 'i', (byte) 'o',
            (byte) 'n', (byte) '=', (byte) '"', (byte) '/');

    private static final Text HTML_FORM2 = Text.constant((byte) '"',
            (byte) ' ', (byte) 'm', (byte) 'e', (byte) 't', (byte) 'h',
            (byte) 'o', (byte) 'd', (byte) '=', (byte) '"', (byte) 'P',
            (byte) 'O', (byte) 'S', (byte) 'T', (byte) '"', (byte) '>',
            (byte) '\n');

    private static final Text HTML_FORM3 = Text.constant((byte) '<',
            (byte) '/', (byte) 'f', (byte) 'o', (byte) 'r', (byte) 'm',
            (byte) '>', (byte) '\n');

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

    private Text id;

    private int rowSpan = 1;

    private HuiNode sibling;

    public void addChild(final HuiNode child) {
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

    public abstract void appendState(OutputStream out) throws IOException;

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

    public Text getState() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write('/');
            StateCodec codec = new StateCodec(out);
            appendState(codec);
            codec.close();
            return Text.constant(out.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    public abstract void loadState(InputStream in) throws IOException;

    public void post(Text value, ActionHandler handler, HuiNode root) {
        // no effect by default
    }

    public abstract void renderNode(OutputStream out) throws IOException;

    public void renderTree(OutputStream out) throws IOException {
        HTML_FORM1.writeTo(out);
        StateCodec codec = new StateCodec(out);
        appendState(codec);
        codec.flush();
        HTML_FORM2.writeTo(out);
        renderNode(out);
        HTML_FORM3.writeTo(out);
    }

    public void setChild(HuiNode child) {
        this.child = child;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public void setId(Text id) {
        this.id = id;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public void setSibling(HuiNode sibling) {
        this.sibling = sibling;
    }

}
