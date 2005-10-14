package de.mobizcorp.femtocms.engine;

import javax.xml.transform.TransformerException;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ResourceNotFound extends TransformerException {

    private static final long serialVersionUID = 6466186513325805468L;

    public ResourceNotFound(String message) {
        super(message);
    }

}
