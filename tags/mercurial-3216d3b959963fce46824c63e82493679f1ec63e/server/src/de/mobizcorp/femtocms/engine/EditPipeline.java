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
