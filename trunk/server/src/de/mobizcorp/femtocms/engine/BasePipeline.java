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
