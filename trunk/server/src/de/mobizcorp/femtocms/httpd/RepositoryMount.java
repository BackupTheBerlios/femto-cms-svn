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
package de.mobizcorp.femtocms.httpd;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.OutputStream;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.Context;
import simple.http.serve.ErrorReport;
import de.mobizcorp.femtocms.engine.BaseEngine;
import de.mobizcorp.femtocms.engine.BasePipeline;
import de.mobizcorp.femtocms.engine.NullEngine;
import de.mobizcorp.femtocms.engine.ResourceNotFound;
import de.mobizcorp.femtocms.engine.StreamResource;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class RepositoryMount extends RepositoryResource {

    private BaseEngine engine;

    private static final String charset = getString(OUTPUT_CHARSET_PREFERENCE,
            OUTPUT_CHARSET_FALLBACK);

    public RepositoryMount(Context ctx, BaseEngine engine) {
        super(ctx);
        this.engine = engine;
    }

    @Override
    protected void process(Request request, Response response) throws Exception {
        long start = System.currentTimeMillis();
        long ims = request.getDate("If-Modified-Since");
        response.setDate("Date", start);
        String value = request.getValue("Pragma");
        if (value != null && value.equals("no-cache")) {
            engine.refresh();
        }
        String path = NullEngine.trimPath(request.getURI());
        int dot = path.lastIndexOf('.');
        if (dot > 0 && !path.startsWith("fckeditor/")) {
            try {
                String type = path.substring(dot + 1);
                String href = path.substring(0, dot) + ".xml";
                if (ims != -1 && engine.getLastModified(href) <= ims) {
                    handle(request, response, 304);
                    return;
                }
                StreamResource text = engine.resolve(href);
                BasePipeline pipeline = engine.newPipeline(type);
                if (pipeline != null && text != null) {
                    pipeline.setParameter("femtocms-request-parameters",
                            request.getParameters());
                    response.set("Content-Type", NullEngine.contentTypeFor(pipeline)
                            + ";charset=" + charset);
                    response.setDate("Last-Modified", text.getLastModified());
                    OutputStream out = response.getPrintStream(8192);
                    try {
                        pipeline.setResult(new StreamResult(out));
                        pipeline.parse(SAXSource.sourceToInputSource(text));
                    } finally {
                        out.close();
                    }
                    response.commit();
                }
            } catch (ResourceNotFound e) {
                // Try to fallback, complain later.
            }
        }

        if (!response.isCommitted()) {
            try {
                if (ims != -1 && engine.getLastModified(path) <= ims) {
                    handle(request, response, 304);
                    return;
                }
                StreamResource source = engine.resolve(path);
                if (source != null) {
                    if (ims != -1 && source.getLastModified() <= ims) {
                        handle(request, response, 304);
                        return;
                    }
                    response.setDate("Last-Modified", source.getLastModified());
                    OutputStream out = response.getOutputStream();
                    NullEngine.copy(source, out);
                    out.close();
                    response.commit();
                } else {
                    handle(request, response, 404);
                    return;
                }
            } catch (ResourceNotFound e) {
                handle(request, response, new ErrorReport(e, 404));
                return;
            }
        }
    }

}
