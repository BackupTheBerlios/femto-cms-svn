package de.mobizcorp.femtocms.model;

import java.io.File;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 * @author Copyright (C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class FileElement implements Element {

    private final File base;

    private final FileElement parent;

    private List children;

    private static final List NO_CHILDREN = new List(new FileElement[0]);

    public static class List implements NodeList {

        private final FileElement entries[];

        public List(FileElement entries[]) {
            this.entries = entries;
        }

        public Node item(int index) {
            try {
                return entries[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }

        public int getLength() {
            return entries.length;
        }

        public int index(Node child) {
            int scan = getLength();
            while (--scan >= 0) {
                if (entries[scan] == child) {
                    return scan;
                }
            }
            return -1;
        }

    }
    
    public FileElement(File base) {
        this.base = base;
        this.parent = null;
    }

    private FileElement(File base, FileElement parent) {
        this.base = base;
        this.parent = parent;
    }

    public String getNodeName() {
        return isFolder() ? "folder" : "file";
    }

    public String getNodeValue() throws DOMException {
        return null;
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                "setNodeValue");
    }

    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    public Node getParentNode() {
        return parent;
    }

    public NodeList getChildNodes() {
        if (children == null) {
            if (isFolder()) {
                File[] files = base.listFiles();
                int scan = files == null ? 0 : files.length;
                FileElement result[] = new FileElement[scan];
                while (--scan >= 0) {
                    result[scan] = new FileElement(files[scan], this);
                }
                children = new List(result);
            } else {
                children = NO_CHILDREN;
            }
        }
        return children;
    }

    public Node getFirstChild() {
        return getChildNodes().item(0);
    }

    public Node getLastChild() {
        return getChildNodes().item(getChildNodes().getLength() - 1);
    }

    public Node getPreviousSibling() {
        if (parent == null) {
            return null;
        }
        return parent.children.item(parent.children.index(this) - 1);
    }

    public Node getNextSibling() {
        if (parent == null) {
            return null;
        }
        return parent.children.item(parent.children.index(this) + 1);
    }

    public NamedNodeMap getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    public Document getOwnerDocument() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node removeChild(Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node appendChild(Node newChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasChildNodes() {
        // TODO Auto-generated method stub
        return false;
    }

    public Node cloneNode(boolean deep) {
        // TODO Auto-generated method stub
        return null;
    }

    public void normalize() {
        // TODO Auto-generated method stub

    }

    public boolean isSupported(String feature, String version) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getNamespaceURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPrefix(String prefix) throws DOMException {
        // TODO Auto-generated method stub

    }

    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasAttributes() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getTextContent() throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setTextContent(String textContent) throws DOMException {
        // TODO Auto-generated method stub

    }

    public boolean isSameNode(Node other) {
        // TODO Auto-generated method stub
        return false;
    }

    public String lookupPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        // TODO Auto-generated method stub
        return false;
    }

    public String lookupNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEqualNode(Node arg) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getFeature(String feature, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getUserData(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean isFolder() {
        return base.isDirectory();
    }

    public String getTagName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAttribute(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttribute(String name, String value) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public void removeAttribute(String name) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public Attr getAttributeNode(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagName(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasAttribute(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        // TODO Auto-generated method stub
        return false;
    }

    public TypeInfo getSchemaTypeInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        // TODO Auto-generated method stub
        
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        // TODO Auto-generated method stub
        
    }

}
