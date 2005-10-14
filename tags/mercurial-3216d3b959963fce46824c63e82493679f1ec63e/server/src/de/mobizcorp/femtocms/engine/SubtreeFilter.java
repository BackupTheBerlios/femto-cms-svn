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
