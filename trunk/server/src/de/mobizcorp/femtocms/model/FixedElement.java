package de.mobizcorp.femtocms.model;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class FixedElement extends FixedNode implements Element {

    public void removeAttribute(String name) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeAttribute");
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeAttributeNode");
    }

    public void removeAttributeNS(String namespaceURI, String localName)
            throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeAttributeNS");
    }

    public void setAttribute(String name, String value) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setAttribute");
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setAttributeNode");
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setAttributeNodeNS");
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName,
            String value) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setAttributeNS");
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setIdAttribute");
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId)
            throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setIdAttributeNode");
    }

    public void setIdAttributeNS(String namespaceURI, String localName,
            boolean isId) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setIdAttributeNS");
    }

}
