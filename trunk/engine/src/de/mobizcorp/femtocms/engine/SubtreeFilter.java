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
package de.mobizcorp.femtocms.engine;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class SubtreeFilter extends BaseFilter {

	private int depth;

	public SubtreeFilter(XMLReader parent, ContentHandler handler) {
		super(parent);
		setContentHandler(handler);
	}

	@Override
	public void characters(char[] ch, int start, int length) {
	}

	@Override
	public void endDocument() throws SAXException {
		if (--depth <= 0) {
			stop();
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (--depth <= 0) {
			stop();
        }
	}

	public final int getDepth() {
		return depth;
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) {
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) {
	}

	@Override
	public void processingInstruction(String target, String data) {
	}

	public final void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void skippedEntity(String name) {
	}

	@Override
	public void startDocument() {
		depth++;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) {
		depth++;
	}

	private void stop() {
		connect(getParent(), getContentHandler());
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) {
	}

	public static SubtreeFilter attach(XMLReader parent, ContentHandler child) {
		SubtreeFilter filter = new SubtreeFilter(parent, child);
        connect(parent, filter);
        return filter;
	}

}
