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
package de.mobizcorp.femtocms.model;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public abstract class FixedAttributeMap implements NamedNodeMap {

    private IndexedAttribute items[];

    private final Element owner;

    public FixedAttributeMap(Element owner) {
        this.owner = owner;

    }

    protected abstract int getIndex(String name);

    protected abstract String getName(int index);

    protected abstract String getValue(int index);

    public static class IndexedAttribute extends FixedAttribute {
        private final FixedAttributeMap map;

        private final int index;

        private String value;

        public IndexedAttribute(FixedAttributeMap map, int index, String value) {
            this.map = map;
            this.index = index;
            this.value = value;
        }

        public String getName() {
            return map.getName(index);
        }

        public boolean getSpecified() {
            return true;
        }

        public String getValue() {
            return value;
        }

        public Element getOwnerElement() {
            return map.owner;
        }

        public TypeInfo getSchemaTypeInfo() {
            throw new UnsupportedOperationException("getSchemaTypeInfo");
        }

        public boolean isId() {
            return "id".equals(getName());
        }

        public String getNodeName() {
            return getName();
        }

        public String getNodeValue() throws DOMException {
            return getValue();
        }

        public short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }

        public Node getParentNode() {
            return getOwnerElement();
        }

        public NodeList getChildNodes() {
            return EmptyNodeList.INSTANCE;
        }

        public Node getFirstChild() {
            return null;
        }

        public Node getLastChild() {
            return null;
        }

        public Node getPreviousSibling() {
            return map.item(index - 1);
        }

        public Node getNextSibling() {
            return map.item(index + 1);
        }

        public NamedNodeMap getAttributes() {
            return null;
        }

        public Document getOwnerDocument() {
            return map.owner.getOwnerDocument();
        }

        public boolean hasChildNodes() {
            return false;
        }

        public boolean isSupported(String feature, String version) {
            return false;
        }

        public String getNamespaceURI() {
            return map.owner.lookupNamespaceURI(getPrefix());
        }

        public String getPrefix() {
            String name = getName();
            int colon = name.indexOf(':');
            return colon == -1 ? null : name.substring(0, colon);
        }

        public String getLocalName() {
            String name = getName();
            int colon = name.indexOf(':');
            return name.substring(colon + 1);
        }

        public boolean hasAttributes() {
            return false;
        }

        public String getBaseURI() {
            return map.owner.getBaseURI();
        }

        public short compareDocumentPosition(Node other) throws DOMException {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "compareDocumentPosition");
        }

        public String getTextContent() throws DOMException {
            // TODO Auto-generated method stub
            return null;
        }

        public String lookupPrefix(String namespaceURI) {
            return map.owner.lookupPrefix(namespaceURI);
        }

        public boolean isDefaultNamespace(String namespaceURI) {
            return map.owner.isDefaultNamespace(namespaceURI);
        }

        public String lookupNamespaceURI(String prefix) {
            return map.owner.lookupNamespaceURI(prefix);
        }

        public boolean isEqualNode(Node other) {
            return this == other;
        }

        public Object getFeature(String feature, String version) {
            return null;
        }

        public Object getUserData(String key) {
            return null;
        }
    }

    public Node getNamedItem(String name) {
        return item(getIndex(name));
    }

    public Node getNamedItemNS(String namespaceURI, String localName)
            throws DOMException {
        return getNamedItem(owner.lookupPrefix(namespaceURI) + ":" + localName);
    }

    public Node item(int index) {
        if (items == null) {
            items = new IndexedAttribute[getLength()];
        }
        try {
            IndexedAttribute result = items[index];
            if (result == null) {
                result = items[index] = new IndexedAttribute(this, index,
                        getValue(index));
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public Node removeNamedItem(String name) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeNamedItem");
    }

    public Node removeNamedItemNS(String namespaceURI, String localName)
            throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "removeNamedItemNS");
    }

    public Node setNamedItem(Node arg) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setNamedItem");
    }

    public Node setNamedItemNS(Node arg) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setNamedItemNS");
    }

}
