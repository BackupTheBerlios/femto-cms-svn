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
import java.util.Iterator;
import java.util.Stack;

import de.mobizcorp.qu8ax.NamePair;
import de.mobizcorp.qu8ax.NamePool;
import de.mobizcorp.qu8ax.Parser;
import de.mobizcorp.qu8ax.Resolver;
import de.mobizcorp.qu8ax.Sink;
import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextBuffer;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Tree builder for HUI.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiBuilder extends Sink {

    private static final Text ID_ROOT = Text.constant((byte) '.', (byte) '0');

    private static final Text ID_SEPARATOR = Text.constant((byte) '.');

    public static HuiNode build(InputStream in) throws IOException {
        HuiBuilder builder = new HuiBuilder();
        Parser parser = new Parser(Resolver.INSTANCE, builder);
        parser.parse(in);
        return builder.getRoot();
    }

    private int ATT_ACTION;

    private int ATT_COLS;

    private int ATT_COLSPAN;

    private int ATT_COLUMNS;

    private int ATT_ID;

    private int ATT_NAME;

    private int ATT_ROWS;

    private int ATT_ROWSPAN;

    private int ATT_SECRET;

    private int ATT_TEXT;

    private final TextBuffer buffer = new TextBuffer();

    private final Stack<HuiNode> eltStack = new Stack<HuiNode>();

    private HuiNode here;

    private HuiNode root;

    private int TAG_BUTTON;

    private int TAG_LABEL;

    private int TAG_PANEL;

    private int TAG_PASSWORDFIELD;

    private int TAG_TEXT;

    private int TAG_TEXTAREA;

    private int TAG_TEXTFIELD;

    public HuiNode getRoot() {
        return root;
    }

    @Override
    public void handleAddAttribute(final int name, final Text value) {
        if (here == null) {
            // parsing unknown element
            return;
        }
        if (name == ATT_ACTION) {
            if (here instanceof HuiButton) {
                ((HuiButton) here).setAction(value);
            }
        } else if (name == ATT_COLS || name == ATT_COLUMNS) {
            if (here instanceof HuiPanel) {
                ((HuiPanel) here).setCols(value.toInt(10));
            } else if (here instanceof HuiText) {
                ((HuiText) here).setCols(value.toInt(10));
            }
        } else if (name == ATT_COLSPAN) {
            here.setColSpan(value.toInt(10));
        } else if (name == ATT_ID || name == ATT_NAME) {
            here.setId(value);
        } else if (name == ATT_ROWS) {
            if (here instanceof HuiText) {
                ((HuiText) here).setRows(value.toInt(10));
            }
        } else if (name == ATT_ROWSPAN) {
            here.setRowSpan(value.toInt(10));
        } else if (name == ATT_SECRET) {
            if (here instanceof HuiText) {
                ((HuiText) here).setSecret(value.size() > 0);
            }
        } else if (name == ATT_TEXT) {
            if (here instanceof HuiLabel) {
                ((HuiLabel) here).setText(value);
            } else if (here instanceof HuiText) {
                ((HuiText) here).setValue(value);
            }
        }
    }

    @Override
    public void handleCharacterData(final boolean parsed, final Text value) {
        buffer.append(value);
    }

    @Override
    public void handleCloseElement(final int name) {
        here = eltStack.pop();
        if (here instanceof HuiLabel) {
            ((HuiLabel) here).setText(buffer.toText());
        }
        buffer.clear();
    }

    @Override
    public void handleOpenDocument(final NamePool<Text> l,
            final NamePool<NamePair> q) {
        final int mt = l.intern(Text.EMPTY);
        Iterator<Text> list = TextLoader.fromXML(HuiBuilder.class);
        this.ATT_ACTION = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_COLS = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_COLSPAN = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_COLUMNS = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_ID = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_NAME = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_ROWS = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_ROWSPAN = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_SECRET = Parser.nameFor(mt, list.next(), q, l);
        this.ATT_TEXT = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_BUTTON = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_LABEL = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_PANEL = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_PASSWORDFIELD = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_TEXT = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_TEXTFIELD = Parser.nameFor(mt, list.next(), q, l);
        this.TAG_TEXTAREA = Parser.nameFor(mt, list.next(), q, l);
    }

    @Override
    public void handleOpenElement(final int name) {
        eltStack.push(here);
        buffer.clear();
    }

    @Override
    public void handleStartElement(final int name) {
        final HuiNode parent = eltStack.empty() ? null : eltStack.peek();
        if (name == TAG_BUTTON) {
            here = new HuiButton();
        } else if (name == TAG_LABEL) {
            here = new HuiLabel();
        } else if (name == TAG_PANEL) {
            here = new HuiPanel();
        } else if (name == TAG_PASSWORDFIELD) {
            HuiText text = new HuiText();
            text.setSecret(true);
            here = text;
        } else if (name == TAG_TEXT || name == TAG_TEXTFIELD
                || name == TAG_TEXTAREA) {
            here = new HuiText();
        } else {
            here = null;
        }
        if (parent == null) {
            if (eltStack.empty()) {
                here.setId(ID_ROOT);
                root = here;
            }
        } else if (here != null) {
            final Text n = Text.valueOf(parent.childCount(), Text.MAX_RADIX);
            here.setId(new TextBuffer().append(parent.getId()).append(
                    ID_SEPARATOR).append(n).toText());
            parent.addChild(here);
        }
    }

    @Override
    public void handleWhitespace(final boolean comment, final Text value) {
        if (!comment) {
            buffer.append(value);
        }
    }
}
