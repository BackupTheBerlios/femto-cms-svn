package de.mobizcorp.femtocms.httpd;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.File;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.serve.Context;
import simple.http.serve.FileContext;

public class RepositoryHandler implements ProtocolHandler {

    private final RepositorySelector selector;

    public RepositoryHandler(Context ctx) {
        this.selector = new RepositorySelector(ctx);
    }

    public RepositoryHandler(String base) {
        this(new FileContext(new File(base)));
    }

    public RepositoryHandler() {
        this(getString(ROOT_MOUNT_PREFERENCE, ROOT_MOUNT_FALLBACK));
    }

    public void handle(Request request, Response response) {
        selector.handle(request, response);
    }

}
