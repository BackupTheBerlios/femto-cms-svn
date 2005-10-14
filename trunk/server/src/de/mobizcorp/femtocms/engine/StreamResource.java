package de.mobizcorp.femtocms.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class StreamResource extends StreamSource {

    private long lastModified;

    private static final ThreadLocal<ResourceLoader> ctx = new ThreadLocal<ResourceLoader>();

    public StreamResource() {
        registerResource(this);
    }

    private static void registerResource(StreamResource resource) {
        ResourceLoader loader = ctx.get();
        if (loader != null) {
            loader.addResource(resource);
        }
    }

    public static void setResourceLoader(ResourceLoader loader) {
        ctx.set(loader);
    }

    public final long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void dispose() {
        dispose(getInputStream());
        dispose(getReader());
    }

    public static void dispose(Reader old) {
        if (old != null) {
            try {
                old.close();
            } catch (IOException e) {
            }
        }
    }

    public static void dispose(InputStream old) {
        if (old != null) {
            try {
                old.close();
            } catch (IOException e) {
            }
        }
    }
}
