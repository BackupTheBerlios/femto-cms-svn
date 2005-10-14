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
