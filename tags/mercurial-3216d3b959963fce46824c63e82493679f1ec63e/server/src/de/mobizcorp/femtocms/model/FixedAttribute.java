package de.mobizcorp.femtocms.model;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class FixedAttribute extends FixedNode implements Attr {

    public void setValue(String value) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setValue");
    }

}
