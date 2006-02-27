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
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ViewPipeline extends NullPipeline {

	private final HashSet<String> includeSet = new HashSet<String>();

	private final Stack<ContentHandler> stack = new Stack<ContentHandler>();

	private String stylesheetTarget = "femtocms-stylesheet";

	public ViewPipeline(NullEngine engine, ContentHandler handler) {
		super(engine, handler);
	}

	public String getStylesheetTarget() {
		return stylesheetTarget;
	}

	protected void processInclude(String href) throws SAXException {
		if (href == null) {
			throw new SAXException("missing href on XInclude");
		}
		if (!includeSet.add(href)) {
			throw new SAXException("circular include: " + href);
		}
		try {
			insert(getEngine().resolve(href, getLocator().getSystemId()));
		} catch (SAXException e) {
			throw e;
		} catch (Exception e) {
			throw new SAXException("including '" + href + "'", e);
		} finally {
			includeSet.remove(href);
		}
	}

	protected boolean pushStylesheet(String data) throws SAXException {
		Map<String, String> atts = NullEngine.readAttributes(data);
		String type = atts.get("type");
		String href = atts.get("href");
		if (type == null || href == null) {
			throw new SAXException("invalid femtocms-stylesheet: " + data);
		} else {
			return pushStylesheet(type, href);
		}
	}

	protected boolean pushStylesheet(String type, String href)
			throws SAXException {
		if (!"text/xsl".equals(type)) {
			return false;
		}
		try {
			ContentHandler oldHandler = getContentHandler();
			XMLFilter transformerFilter = getEngine().newFilter(href);
			transformerFilter.setParent(this);
			FragmentFilter.attach(transformerFilter, oldHandler);
			getContentHandler().startDocument();
			stack.push(oldHandler);
			return true;
		} catch (SAXException e) {
			throw e; // avoid wrap
        } catch (RuntimeException e) {
            throw e;
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	public void setStylesheetTarget(String stylesheetTarget) {
		this.stylesheetTarget = stylesheetTarget;
	}

	@Override
	public void parse(InputSource input) throws SAXException, IOException {
		includeSet.clear();
		super.parse(input);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		if (stylesheetTarget.equals(target) && pushStylesheet(data)) {
			return;
		}
		super.processingInstruction(target, data);
	}

	private void unnest() throws SAXException {
		ContentHandler handler;
		while ((handler = stack.pop()) != null) {
			switchHandler(handler);
		}
	}

	private void switchHandler(ContentHandler contentHandler)
			throws SAXException {
		ContentHandler oldHandler = getContentHandler();
		if (contentHandler == oldHandler) {
			return; // no change
		} else if (oldHandler != null) {
			oldHandler.endDocument();
		}
		setContentHandler(contentHandler);
	}

	@Override
	public void endDocument() throws SAXException {
		unnest();
		super.endDocument();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		stack.push(null);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (Namespace.XMLNS_XI.equals(uri)) {
			if (!"include".equals(localName)) {
				throw new SAXException("unrecognized XInclude element: "
						+ localName);
			}
			String href = atts.getValue("href");
			processInclude(href);
		}
		super.startElement(uri, localName, qName, atts);
		stack.push(null);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		unnest();
		super.endElement(uri, localName, qName);
	}

}
