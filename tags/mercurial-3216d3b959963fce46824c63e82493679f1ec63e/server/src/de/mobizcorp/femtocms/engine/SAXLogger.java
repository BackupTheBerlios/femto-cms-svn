package de.mobizcorp.femtocms.engine;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class SAXLogger extends BaseFilter {

	private static int counter;

	private static final Logger LOGGER = Logger
			.getLogger("de.mobizcorp.femtocms.sax");

	static {
		LOGGER.setLevel(Level.FINEST);
		Logger l = LOGGER;
		while (l != null) {
			Handler[] handlers = l.getHandlers();
			for (Handler h : handlers) {
				h.setLevel(Level.FINEST);
			}
			l = l.getParent();
		}
	}

	private int depth;

	private int id;

	private String indent;

	private Locator locator;

	public SAXLogger(XMLReader parent, ContentHandler child) {
		super(parent);
		setContentHandler(child);
		synchronized (SAXLogger.class) {
			id = ++counter;
		}
		indent = Integer.toHexString(id);
		indent = "0000".substring(indent.length())
				+ indent
				+ " ------------------------------------------------------------"
				+ "-------------------------------------------------------------"
				+ "-------------------------------------------------------------"
				+ "-------------------------------------------------------------";
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		log("characters", ch, start, length);
		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		--depth;
		log("endDocument");
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		--depth;
		logElement("endElement", uri, localName);
		super.endElement(uri, localName, qName);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		log("endPrefixMapping", prefix);
		super.endPrefixMapping(prefix);
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		log("error", e);
		super.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		log("fatalError", e);
		super.fatalError(e);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		log("ignorableWhitespace", ch, start, length);
		super.ignorableWhitespace(ch, start, length);
	}

	private String indent() {
		return indent.substring(0, depth + 5);
	}

	private void log(String op) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1} [{2}]", new Object[] { indent(), op,
					getLocation() });
		}
	}

	private String getLocation() {
		if (locator != null) {
			return EditPipeline.editLocation(locator);
		} else {
			return "unknown::";
		}
	}

	private void log(String op, char[] ch, int start, int length) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1}: ''{2}'' [{3}]",
					new Object[] { indent(), op, new String(ch, start, length),
							getLocation() });
		}
	}

	private void log(String op, Object arg1) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1}: ''{2}'' [{3}]", new Object[] {
					indent(), op, arg1, getLocation() });
		}
	}

	private void log(String op, Object arg1, Object arg2) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1}: ''{2}'',''{3}'' [{4}]",
					new Object[] { indent(), op, arg1, arg2, getLocation() });
		}
	}

	private void log(String op, String arg1, String arg2, String arg3) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1}: ''{2}'',''{3}'',''{4}'' [{5}]",
					new Object[] { indent(), op, arg1, arg2, arg3,
							getLocation() });
		}
	}

	private void log(String op, String arg1, String arg2, String arg3,
			String arg4) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE,
					"{0}{1}: ''{2}'',''{3}'',''{4}'',''{5}'' [{6}]",
					new Object[] { indent(), op, arg1, arg2, arg3, arg4,
							getLocation() });
		}
	}

	private void logAttribute(Attributes atts, int i) {
		LOGGER
				.log(Level.FINE, "{0}{1}: ''{2}:{3}''=''{4}'' [{5}]",
						new Object[] { indent(), "attribute", atts.getURI(i),
								atts.getLocalName(i), atts.getValue(i),
								getLocation() });
	}

	private void logElement(String op, String uri, String localName) {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "{0}{1}: ''{2}:{3}'' [{4}]", new Object[] {
					indent(), op, uri, localName, getLocation() });
		}
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		log("notationDecl", name, publicId, systemId);
		super.notationDecl(name, publicId, systemId);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		log("processingInstruction", target, data);
		super.processingInstruction(target, data);
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		log("resolveEntity", publicId, systemId);
		return super.resolveEntity(publicId, systemId);
	}

	@Override
	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
		log("setProperty", name, value);
		super.setProperty(name, value);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		log("skippedEntity", name);
		super.skippedEntity(name);
	}

	@Override
	public void startDocument() throws SAXException {
		log("startDocument");
		super.startDocument();
		depth++;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		logElement("startElement", uri, localName);
		if (LOGGER.isLoggable(Level.FINE)) {
			int end = atts == null ? 0 : atts.getLength();
			for (int i = 0; i < end; i++) {
				logAttribute(atts, i);
			}
		}
		super.startElement(uri, localName, qName, atts);
		depth++;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		log("startPrefixMapping", prefix, uri);
		super.startPrefixMapping(prefix, uri);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		log("unparsedEntityDecl", name, publicId, systemId, notationName);
		super.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		log("fatalError", e);
		super.warning(e);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		super.setDocumentLocator(locator);
	}

}
