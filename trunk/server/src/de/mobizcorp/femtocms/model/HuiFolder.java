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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import de.mobizcorp.hui.ActionHandler;
import de.mobizcorp.hui.HuiBuilder;
import de.mobizcorp.hui.HuiLabel;
import de.mobizcorp.hui.HuiNode;
import de.mobizcorp.hui.HuiSelect;
import de.mobizcorp.hui.Path;
import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.lib.TextParser;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * Folder browser for femtocms.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiFolder extends HuiNode implements ActionHandler,
        Comparator<File> {

    private static final Text ACTION___, ACTION_CD, ACTION_OPEN, ACTION_SAVE;

    private static final Text HTML_DIV11, HTML_DIV12, HTML_DIV13, HTML_DIV2,
            HTML_DIV3;

    private static final Text ID_CONTENTS, ID_DIR, ID_FILENAME, ID_HISTORY,
            ID_LOCATION;

    private static final Text PATH_SEPARATOR = Text.constant((byte) '/',
            (byte) '\\');

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
        ACTION_SAVE = list.next();
        ID_CONTENTS = list.next();
        ID_DIR = list.next();
        ID_FILENAME = list.next();
        ID_HISTORY = list.next();
        ID_LOCATION = list.next();
    }

    private static File base = new File(System.getProperty("user.dir", "."));

    private transient boolean dirty = true;

    private Text file = Text.EMPTY;

    private Text path = Text.EMPTY;

    public HuiFolder() {
    }

    public HuiFolder(final HuiFolder old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
        this.path = old.path;
        setDirty(old.isDirty());
    }

    public void action(final Text name, final Path<HuiNode> path) {
        final HuiNode context = path.getLast();
        System.out.println("action: '" + name + "' on '" + context.getId()
                + "'");
        if (name.equals(ACTION_OPEN)) {
            final HuiFolder self = (HuiFolder) path.getParent(context);
            final File current = new File(base, self.path.toString());
            final Text elem = ((HuiLabel) context).getText();
            if (elem.equals(ACTION___)) {
                final File parent = current.getParentFile();
                if (parent != null) {
                    self.setPath(Text.valueOf(parent.getPath()));
                }
            } else {
                final File file = new File(current, elem.toString());
                if (file.isDirectory()) {
                    self.setPath(Text.valueOf(file.getPath()));
                } else {
                    self.setFile(elem);
                }
            }
        } else if (name.equals(ACTION_CD)) {
            final HuiSelect select = (HuiSelect) path.getNode().find(
                    ID_LOCATION);
            final HuiFolder self = (HuiFolder) path.getNode().find(ID_DIR);
            final int end = select.getSelected();
            final TextBuffer buffer = new TextBuffer();
            for (int i = 0; i <= end; i++) {
                if (i > 0) {
                    buffer.append((byte) File.separatorChar);
                }
                buffer.append(select.getOption(i));
            }
            self.setPath(buffer.toText());
        } else if (name.equals(ACTION_SAVE)) {
            final HuiEditor editor = (HuiEditor) path.getNode().find(
                    ID_CONTENTS);
            editor.saveValue();
        }
    }

    private void addLink(final Text name, final boolean folder) {
        final HuiLabel link = new HuiLabel();
        link.setId(new TextBuffer().append(getId()).append(
                HuiBuilder.ID_SEPARATOR).append(
                Text.valueOf(childCount(), Text.MAX_RADIX)).toText());
        link.setText(name);
        link.setAction(ACTION_OPEN);
        link.setColSpan(folder ? 2 : 1); // TODO: cheap workaround
        addChild(link);
    }

    public int compare(final File o1, final File o2) {
        if (o1.isDirectory() != o2.isDirectory()) {
            return o1.isDirectory() ? -1 : 1;
        } else {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static File getBase() {
        return HuiFolder.base;
    }

    /**
     * @return Returns the file.
     */
    private Text getFile() {
        return file;
    }

    private final Text getPath() {
        return path;
    }

    private final boolean isDirty() {
        return dirty;
    }

    @Override
    protected void loadState(final InputStream in) throws IOException {
        setPath(loadText(in));
        setFile(loadText(in));
    }

    @Override
    protected void refresh(final HuiNode root) {
        if (isDirty()) {
            setDirty(false);
            refreshList();
            refreshPath(root);
            refreshEditor(root);
            refreshHistory(root);
        }
        super.refresh(root);
    }

    private void refreshEditor(final HuiNode root) {
        File folder = new File(base, path.toString());
        final Text fileName = file.size() == 0 ? Text.EMPTY : Text
                .valueOf(new File(folder, file.toString()).getPath());
        final HuiEditor editor = (HuiEditor) root.find(ID_CONTENTS);
        final HuiLabel label = (HuiLabel) root.find(ID_FILENAME);
        editor.setCurrent(fileName);
        label.setText(fileName);
    }

    private void refreshHistory(HuiNode root) {
        final HuiHistory history = (HuiHistory) root.find(ID_HISTORY);
        history.setFolder(getPath());
        history.setFile(getFile());
    }

    private void refreshList() {
        removeAll();
        final File dir = new File(base, path.toString());
        if (dir.exists() && dir.isDirectory()) {
            addLink(ACTION___, true);
            final File[] files = sort(dir.listFiles());
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                final Text name = Text.valueOf(file.getName());
                if (name.indexOf('.') != 0) {
                    addLink(name, file.isDirectory());
                }
            }
        }
    }

    private void refreshPath(final HuiNode root) {
        final HuiNode node = root.find(ID_LOCATION);
        if (node instanceof HuiSelect) {
            final HuiSelect select = (HuiSelect) node;
            select.clear();
            int count = -1;
            final TextParser tp = new TextParser(getPath(), PATH_SEPARATOR);
            while (tp.hasNext()) {
                select.addOption(tp.next());
                count += 1;
            }
            select.setSelected(count);
        }
    }

    @Override
    public void renderNode(OutputStream out) throws IOException {
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

    @Override
    public void saveState(OutputStream out) throws IOException {
        saveText(getPath(), out);
        saveText(getFile(), out);
    }

    public static void setBase(File base) {
        HuiFolder.base = base;
    }

    private final void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @param file
     *            The file to set.
     */
    private void setFile(Text file) {
        if (!Text.equals(file, this.file)) {
            this.file = file;
            setDirty(true);
        }
    }

    private final void setPath(Text path) {
        if (!Text.equals(path, this.path)) {
            setFile(Text.EMPTY);
            this.path = path;
            setDirty(true);
        }
    }

    private File[] sort(File[] files) {
        if (files != null) {
            Arrays.sort(files, this);
        }
        return files;
    }
}
