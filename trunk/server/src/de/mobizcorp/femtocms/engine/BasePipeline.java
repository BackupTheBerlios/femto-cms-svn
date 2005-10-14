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

import javax.xml.transform.Result;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public interface BasePipeline {
	
	public ContentHandler getContentHandler();

	/**
	 * Get an output property of the next-in-pipeline transformer.
	 * 
	 * @param name
	 *            a property name.
	 * @return the property value, or null.
	 */
	public String getOutputProperty(String name);

	/**
	 * Parse a document.
	 * 
	 * @param input
	 *            the input.
	 * @throws SAXException
	 *             propagated from processing.
	 * @throws IOException
	 *             propagated from I/O.
	 */
	public void parse(InputSource input) throws SAXException, IOException;

	/**
	 * Set a parameter on the next-in-pipeline transformer.
	 * 
	 * @param name
	 *            a parameter name.
	 * @param value
	 *            a value.
	 */
	public void setParameter(String name, Object value);
        
        public void setResult(Result result);
}
