/*
 * femtocms minimalistic content management.
 * Copyright(C) 2006 Klaus Rennecke, all rights reserved.
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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Copyright(C) 2006 Klaus Rennecke, all rights reserved.
 */
public class TransformFilter extends BaseFilter {

    private final TransformerHandler transformerHandler;

    public TransformFilter(final TransformerHandler transformerHandler)
            throws TransformerConfigurationException {
        this.transformerHandler = transformerHandler;
    }

    private XMLReader findParent() throws SAXException {
        if (super.getParent() == null) {
            final XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            setParent(reader);
        }
        return super.getParent();
    }

    public Transformer getTransformer() {
        return transformerHandler.getTransformer();
    }

    @Override
    public void parse(final InputSource input) throws SAXException, IOException {
        findParent().parse(input);
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        transformerHandler.setResult(new SAXResult(handler));
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        // ignored
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        // ignored
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        // ignored
    }

    @Override
    public void setParent(XMLReader parent) {
        super.setParent(parent);
        parent.setContentHandler(transformerHandler);
    }

}
