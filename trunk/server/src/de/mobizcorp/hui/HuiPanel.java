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

import de.mobizcorp.qu8ax.Text;

/**
 * Container HUI element.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiPanel extends HuiNode {

    private static final Text HTML_ELT_CLOSE = Text.constant((byte) '<',
            (byte) '/', (byte) 't', (byte) 'd', (byte) '>');

    private static final Text HTML_ELT_OPEN1 = Text.constant((byte) '<',
            (byte) 't', (byte) 'd', (byte) ' ', (byte) 'c', (byte) 'o',
            (byte) 'l', (byte) 's', (byte) 'p', (byte) 'a', (byte) 'n',
            (byte) '=', (byte) '"');

    private static final Text HTML_ELT_OPEN2 = Text.constant((byte) '"',
            (byte) ' ', (byte) 'r', (byte) 'o', (byte) 'w', (byte) 's',
            (byte) 'p', (byte) 'a', (byte) 'n', (byte) '=', (byte) '"');

    private static final Text HTML_ELT_OPEN3 = Text.constant((byte) '"',
            (byte) '>');

    private static final Text HTML_ROW_CLOSE = Text.constant((byte) '<',
            (byte) '/', (byte) 't', (byte) 'r', (byte) '>');

    private static final Text HTML_ROW_OPEN = Text.constant((byte) '<',
            (byte) 't', (byte) 'r', (byte) '>');

    private static final Text HTML_TABLE_CLOSE = Text.constant((byte) '<',
            (byte) '/', (byte) 't', (byte) 'a', (byte) 'b', (byte) 'l',
            (byte) 'e', (byte) '>');

    private static final Text HTML_TABLE_OPEN = Text.constant((byte) '<',
            (byte) 't', (byte) 'a', (byte) 'b', (byte) 'l', (byte) 'e',
            (byte) '>');

    private int cols;

    @Override
    public void appendState(OutputStream out) throws IOException {
        HuiNode scan = getChild();
        while (scan != null) {
            scan.appendState(out);
            scan = scan.getSibling();
        }
    }

    public int getCols() {
        return cols;
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        HTML_TABLE_OPEN.writeTo(out);
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
        HTML_TABLE_CLOSE.writeTo(out);
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

}