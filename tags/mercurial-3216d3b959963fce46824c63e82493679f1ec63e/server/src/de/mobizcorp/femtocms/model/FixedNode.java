package de.mobizcorp.femtocms.model;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class FixedNode implements Node {

    public Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "appendChild");
    }

    public Node cloneNode(boolean deep) {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "cloneNode");
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "insertBefore");
    }

    public boolean isSameNode(Node other) {
        return this == other;
    }

    public void normalize() {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "normalize");
    }

    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeChild");
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "replaceChild");
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setNodeValue");
    }

    public void setPrefix(String prefix) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setPrefix");
    }

    public void setTextContent(String textContent) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setTextContent");
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setUserData");
    }
}
