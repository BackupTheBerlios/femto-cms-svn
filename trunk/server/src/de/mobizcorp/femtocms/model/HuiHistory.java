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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import de.mobizcorp.hui.HuiText;
import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.水星.Changes;
import de.mobizcorp.水星.Store;
import de.mobizcorp.水星.Changes.LogEntry;

/**
 * History pane for femtocms.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiHistory extends HuiText {
    
    private Text folder;
    
    private Text file;

    public HuiHistory() {
    }

    public HuiHistory(HuiHistory old) throws IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        super(old);
    }

    @Override
    public Text getValue() {
        try {
            return new TextBuffer().append(listHistory()).toText();
        } catch (IOException e) {
            return Text.EMPTY;
        }
    }

    @Override
    protected void renderValue(OutputStream out) throws IOException {
        renderText(out, listHistory());
    }
    
    private InputStream listHistory() throws IOException {
        Changes changes = new Store(new File(folder.toString())).changes();
        LogEntry entry = changes.read(changes.tip());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        entry.writeTo(buffer);
        return new ByteArrayInputStream(buffer.toByteArray());
    }

    public final Text getFile() {
        return file;
    }

    public final void setFile(Text file) {
        this.file = file;
    }

    public final Text getFolder() {
        return folder;
    }

    public final void setFolder(Text folder) {
        this.folder = folder;
    }

}
