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
