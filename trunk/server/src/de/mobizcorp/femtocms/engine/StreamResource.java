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
