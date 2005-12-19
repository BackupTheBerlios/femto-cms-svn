/*
 * femtocms minimalistic content management.
 * Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
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
package de.mobizcorp.femtocms.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import de.mobizcorp.hui.ActionHandler;
import de.mobizcorp.hui.HuiBuilder;
import de.mobizcorp.hui.HuiButton;
import de.mobizcorp.hui.HuiLabel;
import de.mobizcorp.hui.HuiNode;
import de.mobizcorp.hui.HuiSelect;
import de.mobizcorp.hui.HuiText;
import de.mobizcorp.hui.Path;
import de.mobizcorp.hui.RFC2279;
import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextBuffer;
import de.mobizcorp.qu8ax.TextLoader;
import de.mobizcorp.qu8ax.TextParser;

/**
 * Folder browser for femtocms.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiFolder extends HuiNode implements ActionHandler,
        Comparator<File> {

    private static final Text HTML_DIV11, HTML_DIV12, HTML_DIV13, HTML_DIV2,
            HTML_DIV3;

    private static final Text ACTION___, ACTION_CD, ACTION_OPEN;

    private static final Text ID_CONTENTS, ID_DIR, ID_FILENAME, ID_LOCATION;

    static {
        Iterator<Text> list = TextLoader.fromXML(HuiFolder.class);
        HTML_DIV11 = list.next();
        HTML_DIV2 = list.next();
        HTML_DIV12 = list.next();
        HTML_DIV13 = list.next();
        HTML_DIV3 = list.next();
        ACTION___ = list.next();
        ACTION_CD = list.next();
        ACTION_OPEN = list.next();
        ID_CONTENTS = list.next();
        ID_DIR = list.next();
        ID_FILENAME = list.next();
        ID_LOCATION = list.next();
    }

    private Text path = Text.valueOf(System.getProperty("user.dir", "."));

    @Override
    public void saveState(OutputStream out) throws IOException {
        RFC2279.write(path.size(), out);
        path.writeTo(out);
    }

    @Override
    public void loadState(InputStream in) throws IOException {
        final Text newPath = Text.valueOf(in, RFC2279.read(in));
        if (!path.equals(newPath)) {
            path = newPath;
            refresh();
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
        if (getChild() == null) {
            refresh();
        }
        HTML_DIV11.writeTo(out);
        getId().writeTo(out);
        HTML_DIV2.writeTo(out);
        HuiNode scan = getChild();
        while (scan != null) {
            (scan.getColSpan() == 1 ? HTML_DIV12 : HTML_DIV13).writeTo(out);
            scan.renderNode(out);
            HTML_DIV3.writeTo(out);
            scan = scan.getSibling();
        }
        HTML_DIV3.writeTo(out);
    }

    private void refresh() {
        removeAll();
        File dir = new File(path.toString());
        if (dir.exists() && dir.isDirectory()) {
            addButton(ACTION___, true);
            final File[] files = sort(dir.listFiles());
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                final Text name = Text.valueOf(file.getName());
                if (name.indexOf('.') != 0) {
                    addButton(name, file.isDirectory());
                }
            }
        }
    }

    private File[] sort(File[] files) {
        if (files != null) {
            Arrays.sort(files, this);
        }
        return files;
    }

    private void addButton(final Text name, boolean folder) {
        final HuiButton button = new HuiButton();
        button.setId(new TextBuffer().append(getId()).append(
                HuiBuilder.ID_SEPARATOR).append(
                Text.valueOf(childCount(), Text.MAX_RADIX)).toText());
        button.setText(name);
        button.setAction(ACTION_OPEN);
        button.setColSpan(folder ? 2 : 1); // TODO: cheap workaround
        addChild(button);
    }

    private void setPath(Text newPath, Path<HuiNode> context) {
        this.path = newPath;
        refresh();
        HuiNode node = context.getNode().find(ID_LOCATION);
        if (node instanceof HuiSelect) {
            HuiSelect select = (HuiSelect) node;
            select.clear();
            int count = -1;
            TextParser tp = new TextParser(newPath, Text.constant((byte) '/',
                    (byte) '\\'));
            while (tp.hasNext()) {
                select.addOption(tp.next());
                count += 1;
            }
            select.setSelected(count);
        }
    }

    public void action(final Text name, final Path<HuiNode> path) {
        HuiNode context = path.getLast();
        System.out.println("action: '" + name + "' on '" + context.getId()
                + "'");
        if (name.equals(ACTION_OPEN)) {
            HuiFolder self = (HuiFolder) path.getParent(context);
            File current = new File(self.path.toString());
            final Text elem = ((HuiButton) context).getText();
            if (elem.equals(ACTION___)) {
                File parent = current.getParentFile();
                if (parent != null) {
                    self.setPath(Text.valueOf(parent.getPath()), path);
                }
            } else {
                File file = new File(current, elem.toString());
                if (file.isDirectory()) {
                    self.setPath(Text.valueOf(file.getPath()), path);
                } else {
                    HuiText text = (HuiText) path.getNode().find(ID_CONTENTS);
                    HuiLabel label = (HuiLabel) path.getNode()
                            .find(ID_FILENAME);
                    try {
                        final FileInputStream in = new FileInputStream(file);
                        try {
                            text.setValue(TextBuffer.valueOf(in).toText());
                            label.setText(Text.valueOf(file.getPath()));
                        } finally {
                            in.close();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else if (name.equals(ACTION_CD)) {
            HuiSelect select = (HuiSelect) path.getNode().find(ID_LOCATION);
            HuiFolder self = (HuiFolder) path.getNode().find(ID_DIR);
            final int end = select.getSelected();
            TextBuffer buffer = new TextBuffer();
            for (int i = 0; i <= end; i++) {
                if (i > 0) {
                    buffer.append((byte) File.separatorChar);
                }
                buffer.append(select.getOption(i));
            }
            self.setPath(buffer.toText(), path);
        }
    }

    public int compare(File o1, File o2) {
        if (o1.isDirectory() != o2.isDirectory()) {
            return o1.isDirectory() ? -1 : 1;
        } else {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
