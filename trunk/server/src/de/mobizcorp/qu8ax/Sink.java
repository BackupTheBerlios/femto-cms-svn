/*
 * femtocms minimalistic content management.
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
package de.mobizcorp.femtocms.qu8ax;

/**
 * Simple sink, ignoring all incoming events.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class Sink implements Handler {

    public static final Sink INSTANCE = new Sink();

    protected Sink() {
        // Subclass to instantiate
    }

    public void handleAddAttribute(int name, Text value) {
    }

    public void handleCharacterData(boolean parsed, Text value) {
    }

    public void handleCloseDocument() {
    }

    public void handleCloseElement(int name) {
    }

    public void handleInstruction(Text target, Text value) {
    }

    public void handleOpenDocument(NamePool<Text> lNames,
            NamePool<NamePair> qNames) {
    }

    public void handleOpenElement(int name) {
    }

    public void handleStartElement(int name) {
    }

    public void handleWhitespace(boolean comment, Text value) {
    }

}
