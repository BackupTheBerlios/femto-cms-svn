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

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class BaseFilter extends XMLFilterImpl {

	public BaseFilter() {
		super();
	}

	public BaseFilter(XMLReader parent) {
		super(parent);
	}

	public void setContentHandler(ContentHandler handler) {
		super.setContentHandler(handler);
		if (handler instanceof DTDHandler) {
			super.setDTDHandler((DTDHandler) handler);
		}
		if (handler instanceof ErrorHandler) {
			super.setErrorHandler((ErrorHandler) handler);
		}
		if (handler instanceof EntityResolver) {
			super.setEntityResolver((EntityResolver) handler);
		}
	}

	public static void connect(XMLReader reader, ContentHandler handler) {
		reader.setContentHandler(handler);
		if (handler instanceof DTDHandler) {
			reader.setDTDHandler((DTDHandler) handler);
		}
		if (handler instanceof ErrorHandler) {
			reader.setErrorHandler((ErrorHandler) handler);
		}
		if (handler instanceof EntityResolver) {
			reader.setEntityResolver((EntityResolver) handler);
		}
	}
}
