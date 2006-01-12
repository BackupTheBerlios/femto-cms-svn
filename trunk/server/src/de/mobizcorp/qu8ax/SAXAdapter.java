package de.mobizcorp.qu8ax;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.mobizcorp.lib.Text;

public class SAXAdapter extends DefaultHandler {

    private final Handler handler;

    private final OpenPool<Text> lNamePool;

    private final OpenPool<NamePair> qNamePool;

    private int started = -1;

    public SAXAdapter(Handler handler) {
        this.handler = handler;
        this.lNamePool = new OpenPool<Text>();
        this.qNamePool = new OpenPool<NamePair>();
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            if (started != -1) {
                handler.handleOpenElement(started);
                started = -1;
            }
            Text text = Text.valueOf(new String(ch, start, length));
            handler.handleCharacterData(true, text);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            handler.handleCloseDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        try {
            if (started == -1) {
                int name = qNamePool.intern(new NamePair(lNamePool.intern(Text
                        .valueOf(uri)), lNamePool.intern(Text
                        .valueOf(localName))));
                handler.handleCloseElement(name);
            } else {
                started = -1;
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            if (started != -1) {
                handler.handleOpenElement(started);
                started = -1;
            }
            Text text = Text.valueOf(new String(ch, start, length));
            handler.handleWhitespace(false, text);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        try {
            if (started != -1) {
                handler.handleOpenElement(started);
                started = -1;
            }
            handler.handleInstruction(Text.valueOf(target), Text.valueOf(data));
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            handler.handleOpenDocument(lNamePool, qNamePool);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        try {
            if (started != -1) {
                handler.handleOpenElement(started);
            }
            int name = qNamePool.intern(new NamePair(lNamePool.intern(Text
                    .valueOf(uri)), lNamePool.intern(Text.valueOf(localName))));
            handler.handleStartElement(name);
            started = name;
            int end = atts == null ? 0 : atts.getLength();
            for (int i = 0; i < end; i++) {
                name = qNamePool.intern(new NamePair(lNamePool.intern(Text
                        .valueOf(atts.getURI(i))), lNamePool.intern(Text
                        .valueOf(atts.getLocalName(i)))));
                handler
                        .handleAddAttribute(name, Text
                                .valueOf(atts.getValue(i)));
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

}
