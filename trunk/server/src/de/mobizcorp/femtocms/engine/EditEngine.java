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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import javax.xml.transform.sax.TransformerHandler;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class EditEngine extends NullEngine {
    // This is a dot because a type cannot contain a dot.
    public static final String FCM_SAVE_TYPE = ".";

    public EditEngine(File base) {
        super(base);
        methods.put("html", "femtocms:/edit.xsl");
        methods.put(FCM_SAVE_TYPE, "femtocms:/save.xsl");
    }

    @Override
    protected StreamResource createStreamSource(String href) throws IOException {
        File fetch = new File(base, href);
        if (fetch.exists()) {
            StreamResource result = new StreamResource();
            result.setLastModified(fetch.lastModified());
            result.setSystemId(fetch.toURI().toString());
            result.setInputStream(new FileInputStream(fetch));
            return result;
        }
        return super.createStreamSource(href);
    }

    @Override
    public long getLastModified(String href) {
        try {
            String path = relativize(this.baseUri, new URI(href));
            if (path.startsWith("preview:")) {
                path = path.substring(8);
            }
            File fetch = new File(base, path);
            if (fetch.exists()) {
                return fetch.lastModified();
            }
        } catch (Exception e) {
        }
        return super.getLastModified(href);
    }

    @Override
    public NullPipeline newPipeline(String method, TransformerHandler handler) {
        return method == FCM_SAVE_TYPE ? new SavePipeline(this, handler)
                : new EditPipeline(this, handler);
    }

}
