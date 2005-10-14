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
import javax.xml.transform.Source;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class NullPipeline extends BaseFilter implements BasePipeline {

	private final NullEngine engine;

	private boolean filter;

	private Locator locator;

	public NullPipeline(NullEngine engine, ContentHandler handler) {
		this.engine = engine;
		setContentHandler(handler);
		setEntityResolver(engine);
	}

	public final NullEngine getEngine() {
		return engine;
	}

	public final Locator getLocator() {
		return locator;
	}

	public String getOutputProperty(String name) {
		TransformerHandler th = getTransformerHandler();
		return th == null ? null : th.getTransformer().getOutputProperty(name);
	}

	protected TransformerHandler getTransformerHandler() {
		ContentHandler handler = getContentHandler();
		do {
			if (handler instanceof TransformerHandler) {
				return ((TransformerHandler) handler);
			}
		} while (handler instanceof BaseFilter
				&& (handler = ((BaseFilter) handler).getContentHandler()) != null);
		return null;
	}

	protected void insert(Source source) throws SAXException, IOException {
		Locator outerLocator = this.locator;
		boolean outerFilter = this.filter;
		try {
			this.filter = false;
			FragmentFilter.parse(source, this);
		} finally {
			setDocumentLocator(outerLocator);
			this.filter = outerFilter;
		}
	}

	public final boolean isFilter() {
		return filter;
	}

	@Override
	public void parse(InputSource input) throws SAXException, IOException {
		if (getParent() == null) {
			setParent(XMLReaderFactory.createXMLReader());
		}
		getParent().setEntityResolver(engine);
		super.parse(input);
	}

	protected void replace(Source source) throws SAXException, IOException {
		SubtreeFilter.attach(getParent(), this).setDepth(1);
		insert(source);
		filter = true;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		super.setDocumentLocator(locator);
	}

	public void setParameter(String name, Object value) {
		getTransformerHandler().getTransformer().setParameter(name, value);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (filter) {
            filter = false;
        } else {
			super.startElement(uri, localName, qName, atts);
		}
	}

    public void setResult(Result result) {
        getTransformerHandler().setResult(result);
    }
}
