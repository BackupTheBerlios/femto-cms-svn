/*
 * Half User Interface.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
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
package de.mobizcorp.hui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.mobizcorp.qu8ax.SAXAdapter;

/**
 * Source implementation to generate a tree instance.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class HuiSource implements Source {

    private static HuiNode buildFrom(InputStream byteStream) throws IOException {
        return HuiBuilder.build(byteStream);
    }

    private static HuiNode buildFrom(SAXSource source) throws IOException,
            SAXException {
        InputSource input = source.getInputSource();
        if (source.getXMLReader() != null) {
            return buildFrom(source.getXMLReader(), input);
        }
        if (input.getCharacterStream() != null) {
            return buildFrom(XMLReaderFactory.createXMLReader(), input);
        } else if (input.getByteStream() != null) {
            return buildFrom(input.getByteStream());
        } else {
            return buildFrom(input.getSystemId());
        }
    }

    private static HuiNode buildFrom(StreamSource source) throws IOException,
            SAXException {
        try {
        if (source.getReader() != null) {
            return buildFrom(XMLReaderFactory.createXMLReader(), SAXSource
                    .sourceToInputSource(source));
        } else if (source.getInputStream() != null) {
            return buildFrom(source.getInputStream());
        } else {
            return buildFrom(source.getSystemId());
        }
        } finally {
            if (source.getReader() != null) {
                source.getReader().close();
            }
            if (source.getInputStream() != null) {
                source.getInputStream().close();
            }
        }
    }

    private static HuiNode buildFrom(String systemId) throws IOException {
        if (systemId.indexOf("://") != -1) {
            return buildFrom(new URL(systemId).openStream());
        } else {
            return buildFrom(new FileInputStream(systemId));
        }
    }

    private static HuiNode buildFrom(XMLReader reader, InputSource input)
            throws IOException, SAXException {
        HuiBuilder builder = new HuiBuilder();
        SAXAdapter adapter = new SAXAdapter(builder);
        reader.setContentHandler(adapter);
        reader.parse(input);
        return builder.getRoot();
    }

    private final Source source;

    private transient HuiNode tree;

    public HuiSource(Source source) {
        this.source = source;
    }

    public HuiSource(String systemId) {
        this(new StreamSource(systemId));
    }

    public String getSystemId() {
        return source.getSystemId();
    }

    public HuiNode instance() throws IOException {
        try {
            if (tree == null) {
                if (source instanceof StreamSource) {
                    tree = buildFrom((StreamSource) source);
                } else if (source instanceof SAXSource) {
                    tree = buildFrom((SAXSource) source);
                }
            }
            return tree.copy();
        } catch (Exception e) {
            IOException ioe = new IOException(e.toString());
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void setSystemId(String systemId) {
        source.setSystemId(systemId);
    }

}
