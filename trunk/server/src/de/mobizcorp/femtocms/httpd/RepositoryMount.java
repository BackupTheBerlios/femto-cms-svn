package de.mobizcorp.femtocms.httpd;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.EXPIRE_FACTOR_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.EXPIRE_FACTOR_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getDouble;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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

    private static double expireFactor = getDouble(EXPIRE_FACTOR_PREFERENCE,
            EXPIRE_FACTOR_FALLBACK);

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
                    OutputStream out = response.getPrintStream(8192);
                    pipeline.setResult(new StreamResult(out));
                    response.set("Content-Type", contentTypeFor(pipeline)
                            + ";charset=" + charset);
                    response.setDate("Last-Modified", text.getLastModified());
                    pipeline.parse(SAXSource.sourceToInputSource(text));
                    long took = System.currentTimeMillis() - start;
                    response.set("FCM-Render-Time", took + "ms");
                    response.setDate("Expires",
                            (long) (start + (took * expireFactor)));
                    response.commit();
                    out.close();
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
                    copy(source, out);
                    out.close();
                } else {
                    handle(request, response, 404);
                    return;
                }
                response.commit();
            } catch (ResourceNotFound e) {
                handle(request, response, new ErrorReport(e, 404));
                return;
            }
        }
    }

    private static String contentTypeFor(BasePipeline pipeline) {
        String value = pipeline.getOutputProperty("media-type");
        if (value != null) {
            return value;
        }
        value = pipeline.getOutputProperty("method");
        if (value == null || value.equals("text")) {
            return "text/plain";
        } else {
            return "text/" + value;
        }
    }

    private static void copy(Source source, OutputStream out)
            throws IOException {
        int n;
        byte[] buffer = new byte[8192];
        InputStream in = ((StreamSource) source).getInputStream();
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
    }

}
