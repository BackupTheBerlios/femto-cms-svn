package de.mobizcorp.femtocms.model;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class EmptyNodeList implements NodeList {

    public static final EmptyNodeList INSTANCE = new EmptyNodeList();

    private EmptyNodeList() {
    }

    public Node item(int index) {
        return null;
    }

    public int getLength() {
        return 0;
    }

}
