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
import java.net.URI;

import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class BaseEngine implements EntityResolver {
    public static String modified(Object engine) {
        return ((BaseEngine) engine).modified();
    }
    
    public static long modified(Object engine, String href) {
        return ((BaseEngine) engine).getLastModified(href);
    }

    public static String push(Object engine, Node data) {
        return ((BaseEngine) engine).push(data);
    }

    public abstract URI getBaseUri();

    public abstract long getLastModified(String href);

    public abstract String modified();

    public abstract NullPipeline newPipeline(String style)
            throws TransformerException;

    public abstract String push(Node data);

    public abstract void refresh();

    public abstract StreamResource resolve(String href) throws TransformerException;

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        try {
            return SAXSource.sourceToInputSource(resolve(systemId));
        } catch (TransformerException e) {
            throw new SAXException(e);
        }
    }
}
