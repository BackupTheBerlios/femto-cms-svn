package de.mobizcorp.femtocms.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class FragmentFilter extends BaseFilter {

    private final HashMap<String, String> mappings = new HashMap<String, String>();

    private boolean filter;

    public static void attach(XMLReader parent, ContentHandler child) {
        connect(parent, new FragmentFilter(parent, child));
    }

    public static void parse(Source source, ContentHandler child)
            throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        FragmentFilter filter = new FragmentFilter(reader, child);
        filter.parse(SAXSource.sourceToInputSource(source));
    }

    public FragmentFilter(XMLReader parent, ContentHandler handler) {
        super(parent);
        setContentHandler(handler);
    }

    @Override
    public void endDocument() throws SAXException {
        filter = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!Namespace.match(uri, localName, Namespace.XMLNS_FCM, "fragment")) {
            super.endElement(uri, localName, qName);
            filter = false;
        } else {
            filter = true;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        mappings.clear();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (!Namespace.match(uri, localName, Namespace.XMLNS_FCM, "fragment")) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                super.startPrefixMapping(entry.getKey(), entry.getValue());
            }
            super.startElement(uri, localName, qName, atts);
        }
        mappings.clear();
    }

    public static String wrap(String text) {
        return "<!DOCTYPE femtocms:fragment [\n" +
                        "<!ENTITY % html32 SYSTEM 'femtocms:/html32.txt'>\n" +
                        "%html32;\n" +
                        "]>\n"
                + "<femtocms:fragment xmlns:femtocms=\"" + Namespace.XMLNS_FCM
                + "\">" + text + "</femtocms:fragment>";
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (!filter) {
            super.endPrefixMapping(prefix);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        // We must not emit prefix mappings before we know that we
        // will emit the associated element.
        mappings.put(prefix, uri);
    }
}
