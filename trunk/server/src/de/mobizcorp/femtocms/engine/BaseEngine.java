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
