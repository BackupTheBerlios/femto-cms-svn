/*
 * Quick UTF-8 API for XML.
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
package de.mobizcorp.qu8ax;

import java.io.IOException;

import de.mobizcorp.lib.Text;

/**
 * Simple generic filter base class.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class Filter implements Handler {

    private final Handler delegate;

    public Filter(Handler delegate) {
        this.delegate = delegate == null ? Sink.INSTANCE : delegate;
    }

    public Handler getDelegate() {
        return delegate;
    }

    public void handleAddAttribute(int name, Text value) throws IOException {
        delegate.handleAddAttribute(name, value);
    }

    public void handleCharacterData(boolean parsed, Text value)
            throws IOException {
        delegate.handleCharacterData(parsed, value);
    }

    public void handleCloseDocument() throws IOException {
        delegate.handleCloseDocument();
    }

    public void handleCloseElement(int name) throws IOException {
        delegate.handleCloseElement(name);
    }

    public void handleInstruction(Text target, Text value) throws IOException {
        delegate.handleInstruction(target, value);
    }

    public void handleOpenDocument(NamePool<Text> lNames,
            NamePool<NamePair> qNames) throws IOException {
        delegate.handleOpenDocument(lNames, qNames);
    }

    public void handleOpenElement(int name) throws IOException {
        delegate.handleOpenElement(name);
    }

    public void handleStartElement(int name) throws IOException {
        delegate.handleStartElement(name);
    }

    public void handleWhitespace(boolean comment, Text value)
            throws IOException {
        delegate.handleWhitespace(comment, value);
    }
}
