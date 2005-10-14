package de.mobizcorp.femtocms.engine;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public interface ResourceLoader {

    public void addResource(StreamResource resource);

    public void disposeAll();

}
