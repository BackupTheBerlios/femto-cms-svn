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

/**
 * Base class for all HUI elements.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public abstract class HuiNode {
    private HuiNode child;

    private int colSpan;

    private int rowSpan;

    private HuiNode sibling;

    public abstract void appendState(OutputStream out) throws IOException;

    public HuiNode getChild() {
        return child;
    }

    public int getColSpan() {
        return colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public HuiNode getSibling() {
        return sibling;
    }

    public abstract void renderNode(OutputStream out) throws IOException;

    public void setChild(HuiNode child) {
        this.child = child;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public void setSibling(HuiNode sibling) {
        this.sibling = sibling;
    }
}
