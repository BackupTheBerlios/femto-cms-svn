package de.mobizcorp.femtocms.httpd;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.Context;
import simple.http.serve.StatusReport;
import de.mobizcorp.femtocms.engine.BaseEngine;
import de.mobizcorp.femtocms.engine.BasePipeline;
import de.mobizcorp.femtocms.engine.EditEngine;
import de.mobizcorp.femtocms.engine.StreamResource;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class RepositoryWriter extends RepositoryResource {

    private BaseEngine engine;

    public RepositoryWriter(Context ctx, BaseEngine engine) {
        super(ctx);
        this.engine = engine;
    }

    @Override
    protected void process(Request request, Response response) throws Exception {
        String editId = request.getParameter("edit-id");
        if (editId == null) {
            handle(request, response, 403);
            return;
        }
        String source = request.getParameter("fckeditor0");
        if (source == null) {
            source = "<!-- DELETED -->";
        }
        int colon = editId.lastIndexOf(':');
        if (colon == -1) {
            handle(request, response, 403);
            return;
        }
        colon = editId.lastIndexOf(':', colon - 1);
        if (colon == -1) {
            handle(request, response, 403);
            return;
        }
        String href = editId.substring(0, colon);
        StreamResource text = engine.resolve(href);
        long lastModified = engine.getLastModified(href);
        if (lastModified == 0) {
            // Not in repository space.
            handle(request, response, 403);
            return;
        }
        long editLastModified = Long.parseLong(request
                .getParameter("edit-lastmodified"));
        if (lastModified > editLastModified) {
            handle(request, response, new StatusReport(403) {

                @Override
                public String getText() {
                    return "Concurrent modification";
                }

            });
            return;
        }
        URI output = new URI(text.getSystemId());
        if (engine.getBaseUri().relativize(output).isAbsolute()) {
            // Not in repository space.
            handle(request, response, 403);
            return;
        }
        File file = new File(output);
        File newFile = new File(file.getParentFile(), file.getName() + ".new");
        File oldFile = new File(file.getParentFile(), file.getName() + "~");
        FileOutputStream out = new FileOutputStream(newFile);
        try {
            BasePipeline pipeline = engine.newPipeline(
                    EditEngine.FCM_SAVE_TYPE);
            pipeline.setParameter(editId, source);
            pipeline.setResult(new StreamResult(out));
            pipeline.parse(SAXSource.sourceToInputSource(text));
        } finally {
            out.close();
        }
        if ((oldFile.exists() && !oldFile.delete()) || !file.renameTo(oldFile)
                || !newFile.renameTo(file)) {
            handle(request, response, new StatusReport(403) {

                @Override
                public String getText() {
                    return "File change failed";
                }

            });
            return;
        }
        response.set("Location", request.getURI());
        handle(request, response, 302);
    }
}
