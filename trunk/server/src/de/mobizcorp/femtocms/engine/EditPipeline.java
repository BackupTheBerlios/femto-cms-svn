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

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class EditPipeline extends ViewPipeline {

	public static String editId(Locator locator) {
		return editId(locator.getSystemId(), locator.getLineNumber(), locator
				.getColumnNumber());
	}

	public static String editId(String systemId, int line, int column) {
		return systemId + ':' + line + ':' + column;
	}

	public static String editLocation(Locator locator) {
		return editLocation(locator.getSystemId(), locator.getLineNumber(),
				locator.getColumnNumber());
	}

	public static String editLocation(String systemId, int line, int column) {
		return "system=\"" + systemId + "\" line=\"" + line + "\" column=\""
				+ column + "\"";
	}

	public EditPipeline(NullEngine engine, ContentHandler handler) {
		super(engine, handler);
	}

	private String editLocation() {
		return editLocation(getLocator());
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		super.startElement(uri, localName, qName, atts);
		if (getLocator() != null) {
			getContentHandler().processingInstruction("femtocms-edit-location",
					editLocation());
		}
	}
}
