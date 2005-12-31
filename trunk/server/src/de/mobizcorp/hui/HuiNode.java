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

import de.mobizcorp.qu8ax.Resolver;
import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextInputStream;
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

    public static void saveText(final Text text, final OutputStream out)
            throws IOException {
        RFC2279.write(text.size(), out);
        text.writeTo(out);
    }
    
    public static Text loadText(InputStream in) throws IOException {
        return Text.valueOf(in, RFC2279.read(in));
    }

    public static void renderText(final OutputStream out, final InputStream in)
            throws IOException {
        int b;
        while ((b = in.read()) != -1) {
            switch (b) {
            case '"':
                Resolver.ENT_QUOT.writeTo(out);
                break;
            case '&':
                Resolver.ENT_AMP.writeTo(out);
                break;
            case '<':
                Resolver.ENT_LT.writeTo(out);
                break;
            case '>':
                Resolver.ENT_GT.writeTo(out);
                break;
            default:
                out.write(b);
                break;
            }
        }
    }

    public static void renderText(final OutputStream out, final Text text)
            throws IOException {
        if (text != null) {
            renderText(out, new TextInputStream(text));
        }
    }

    private HuiNode child;

    private int colSpan = 1;

    private boolean enabled = true;

    private Text id;

    private int rowSpan = 1;

    private HuiNode sibling;

    public HuiNode() {
    }

    /**
     * Copy constructor creating deep copies. Each subclass must implement a
     * constructor of prototype T(T old).
     * 
     * @param old
     *            node structure to copy.
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws IllegalArgumentException
     *             when a constructor in the sub-tree fails.
     */
    public HuiNode(HuiNode old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        setColSpan(old.getColSpan());
        setEnabled(old.isEnabled());
        setId(old.getId());
        setRowSpan(old.getRowSpan());
        HuiNode scan = old.getChild();
        while (scan != null) {
            addChild(scan.copy());
            scan = scan.getSibling();
        }
    }

    public HuiNode copy() throws IllegalArgumentException, SecurityException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Class<? extends HuiNode> t = getClass();
        return t.getConstructor(t).newInstance(this);
    }

    /**
     * Add a new child to this node.
     * 
     * @param child
     *            a node.
     * @throws IllegalArgumentException
     *             if the given node is an ancestor or this.
     */
    public void addChild(final HuiNode child) {
        if (isAncestorOrSelf(child)) {
            throw new IllegalArgumentException("add ancestor or self");
        }
        HuiNode scan = getChild();
        if (scan == null) {
            setChild(child);
        } else {
            HuiNode next = scan.getSibling();
            while (next != null) {
                next = (scan = next).getSibling();
            }
            scan.setSibling(child);
        }
    }

    private void setSibling(HuiNode sibling) {
        this.sibling = sibling;
    }

    private void setChild(HuiNode child) {
        this.child = child;
    }

    /**
     * @return the number of children of this node.
     */
    public int childCount() {
        int n = 0;
        HuiNode scan = getChild();
        while (scan != null) {
            n += 1;
            scan = scan.getSibling();
        }
        return n;
    }

    protected void refresh(HuiNode root) {
        HuiNode scan = getChild();
        while (scan != null) {
            scan.refresh(root);
            scan = scan.getSibling();
        }
    }

    /**
     * Find a node with the given <var>id</var> in the sub-tree starting at
     * this node.
     * 
     * @param id
     *            an id.
     * @return the node found, or null.
     */
    public HuiNode find(final Text id) {
        if (id.equals(getId())) {
            return this;
        }
        HuiNode scan = getChild();
        while (scan != null) {
            HuiNode found = scan.find(id);
            if (found != null) {
                return found;
            }
            scan = scan.getSibling();
        }
        return null;
    }

    /**
     * @return the first child of this node, or null.
     */
    public final HuiNode getChild() {
        return child;
    }

    /**
     * @return the number of colums spanned by this node.
     */
    public final int getColSpan() {
        return colSpan;
    }

    /**
     * @return the id of this node.
     */
    public final Text getId() {
        return id;
    }

    /**
     * @return the number of rows spanned by this node.
     */
    public final int getRowSpan() {
        return rowSpan;
    }

    /**
     * @return the next sibling of this node.
     */
    public final HuiNode getSibling() {
        return sibling;
    }

    /**
     * Answer true if the given <var>parent</var> is an ancestor, or this.
     * 
     * @param node
     *            a node.
     * @return true iff the node is an ancestor or this.
     */
    public final boolean isAncestorOrSelf(final HuiNode node) {
        if (this == node) {
            return true;
        }
        HuiNode scan = node.getChild();
        while (scan != null) {
            if (isAncestorOrSelf(scan)) {
                return true;
            } else {
                scan = scan.getSibling();
            }
        }
        return false;
    }

    /**
     * @return true iff this node is enabled for user interaction.
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Load the node state from the given input stream.
     * 
     * @param in
     *            an input stream.
     * @throws IOException
     *             propagated from I/O.
     */
    protected abstract void loadState(InputStream in) throws IOException;
    
    public void readState(InputStream in) throws IOException {
        loadState(in);
        refresh(this);
    }

    /**
     * @param id
     *            a node id.
     * @return the path from this node to the node with the given id, or null.
     */
    public Path<HuiNode> path(final Text id) {
        if (id.equals(getId())) {
            return new Path<HuiNode>(this, null);
        }
        HuiNode scan = getChild();
        while (scan != null) {
            Path<HuiNode> found = scan.path(id);
            if (found != null) {
                return new Path<HuiNode>(this, found);
            }
            scan = scan.getSibling();
        }
        return null;
    }

    public void post(Text value, ActionHandler handler, Path<HuiNode> path) {
        // no effect by default
    }

    /**
     * Remove all children from this node.
     */
    public void removeAll() {
        HuiNode scan = getChild();
        setChild(null);
        while (scan != null) {
            HuiNode next = scan.getSibling();
            scan.setSibling(null);
            scan = next;
        }
    }

    /**
     * Remove the given child from this node. This method does nothing if
     * <var>child</var> is not a direct child of this node.
     * 
     * @param child
     *            a child of this node.
     */
    public void removeChild(HuiNode child) {
        HuiNode scan = getChild();
        if (scan == child) {
            setChild(child.getSibling());
            child.setSibling(null);
            return;
        }
        while (scan != null) {
            if (scan.getSibling() == child) {
                scan.setSibling(child.getSibling());
                child.setSibling(null);
                break;
            }
            scan = scan.getSibling();
        }
    }

    /**
     * Render this node to the output stream.
     * 
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
    public abstract void renderNode(OutputStream out) throws IOException;

    /**
     * Render a HTML page with the tree starting at this node to the given
     * output stream.
     * 
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
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

    /**
     * Render the tree starting at this node to the given output stream.
     * 
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
    public void renderTree(OutputStream out) throws IOException {
        HTML_FORM1.writeTo(out);
        StateCodec codec = new StateCodec(out);
        refresh(this);
        saveState(codec);
        codec.flush();
        HTML_FORM2.writeTo(out);
        renderNode(out);
        HTML_FORM3.writeTo(out);
    }

    /**
     * Save the current state to the given output stream.
     * 
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
    public abstract void saveState(OutputStream out) throws IOException;

    /**
     * @param colSpan
     *            the number of columns to span by this node.
     */
    public final void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    /**
     * @param enabled
     *            true iff this node should be enabled for user interaction.
     */
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @param id
     *            the id for this node, should be unique in a tree (unchecked).
     */
    public final void setId(Text id) {
        this.id = id;
    }

    /**
     * @param rowSpan
     *            the number of rows to span by this node.
     */
    public final void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    /**
     * Write the state of the sub-tree starting at this node to the output
     * stream, encoded as base64.
     * 
     * @param out
     *            an output stream.
     * @throws IOException
     *             propagated from I/O.
     */
    public void writeState(OutputStream out) throws IOException {
        refresh(this);
        out.write('/');
        StateCodec codec = new StateCodec(out);
        saveState(codec);
        codec.flush();
    }

}
