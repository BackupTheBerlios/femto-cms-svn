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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import de.mobizcorp.hui.HuiText;
import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;

/**
 * Editor pane for femtocms.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiEditor extends HuiText {

    private static void copy(File inFile, File outFile) throws IOException {
        FileInputStream in = new FileInputStream(inFile);
        try {
            FileOutputStream out = new FileOutputStream(outFile);
            try {
                int n;
                byte[] buffer = new byte[8192];
                while ((n = in.read(buffer)) != -1) {
                    if (n > 0) {
                        out.write(buffer, 0, n);
                    }
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private Text current = Text.EMPTY;

    public HuiEditor() {
    }

    public HuiEditor(HuiEditor old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
    }

    /**
     * @return Returns the current file name.
     */
    public final Text getCurrent() {
        return current;
    }

    @Override
    public Text getValue() {
        if (current == null || current.size() == 0) {
            return Text.EMPTY;
        }
        try {
            final FileInputStream in = new FileInputStream(current.toString());
            try {
                return TextBuffer.valueOf(in).toText();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            return Text.EMPTY;
        }
    }

    @Override
    protected void renderValue(OutputStream out) throws IOException {
        if (current == null || current.size() == 0) {
            return;
        }
        final FileInputStream in = new FileInputStream(current.toString());
        try {
            renderText(out, new BufferedInputStream(in));
        } finally {
            in.close();
        }
    }

    /**
     * @param current
     *            The current file name to set.
     */
    public final void setCurrent(Text current) {
        this.current = current;
    }

    public void saveValue() {
        if (current == null || current.size() == 0) {
            return;
        }
        try {
            final File oldFile = new File(current.toString());
            final File newFile = File.createTempFile(oldFile.getName(), ".new",
                    oldFile.getParentFile());
            final File bakFile = File.createTempFile(oldFile.getName(), ".bak",
                    oldFile.getParentFile());
            final FileOutputStream out = new FileOutputStream(newFile);
            try {
                super.getValue().writeTo(out);
            } finally {
                out.close();
            }
            if (oldFile.renameTo(bakFile)) {
                if (newFile.renameTo(oldFile)) {
                    bakFile.delete();
                    return;
                }
            } else {
                copy(oldFile, bakFile);
            }
            // renaming does not work - use copy
            copy(newFile, oldFile);
            newFile.delete();
            bakFile.delete();
        } catch (NullPointerException e) {
            // no current file set
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
