package de.mobizcorp.femtocms.httpd;

import java.util.Stack;
import java.util.logging.Logger;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.BasicResource;
import simple.http.serve.Context;
import simple.http.serve.Report;
import de.mobizcorp.femtocms.engine.ResourceLoader;
import de.mobizcorp.femtocms.engine.StreamResource;

public abstract class RepositoryResource extends BasicResource implements
        ResourceLoader {

    private final Stack<StreamResource> loaded = new Stack<StreamResource>();

    public RepositoryResource(Context ctx) {
        super(ctx);
    }

    @Override
    public void handle(Request req, Response resp) {
        try {
            StreamResource.setResourceLoader(this);
            super.handle(req, resp);
        } finally {
            disposeAll();
        }
    }

    public final void addResource(StreamResource resource) {
        loaded.push(resource);
    }

    public final void disposeAll() {
        while (!loaded.isEmpty()) {
            loaded.pop().dispose();
        }
    }

    @Override
    protected void process(Request req, Response resp, Report report)
            throws Exception {
        Logger.getLogger("de.mobizcorp.femtocms.httpd").warning(
                report.getText() + " " + report.getCause());
        super.process(req, resp, report);
    }
}
