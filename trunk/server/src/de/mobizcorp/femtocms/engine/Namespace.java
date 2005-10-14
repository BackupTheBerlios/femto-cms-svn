package de.mobizcorp.femtocms.engine;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class Namespace {

	private Namespace() {
	}

	public static final String XMLNS_FCM = "http://xml.apache.org/xalan/java/de.mobizcorp.femtocms.engine.BaseEngine";
	
	public static final String XMLNS_XI = "http://www.w3.org/2001/XInclude";

	public static boolean match(String uri, String localName, String ns, String name) {
		return ns.equals(uri) && name.equals(localName);
	}
}
