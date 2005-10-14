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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class SavePipeline extends NullPipeline {

	private final HashMap<String, String> stringParameters = new HashMap<String, String>();

	public SavePipeline(NullEngine engine, ContentHandler handler) {
		super(engine, handler);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String editId = EditPipeline.editId(getLocator());
		String parameter = stringParameters.get(editId);
		if (parameter != null) {
			StreamSource source = new StreamSource(new StringReader(
					FragmentFilter.wrap(parameter)), editId);
			try {
				replace(source);
			} catch (IOException e) {
				throw new SAXException("replacing " + editId, e);
			}
		}
		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void setParameter(String name, Object value) {
		if (value instanceof String) {
			stringParameters.put(name, (String) value);
		}
		super.setParameter(name, value);
	}

}
