package de.mobizcorp.femtocms.httpd;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.FCM_SERVER_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.FCM_SERVER_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.LOG_REQUEST_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.LOG_REQUEST_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.WRITE_AUTH_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.WRITE_AUTH_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.File;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.BasicResource;
import simple.http.serve.Context;
import simple.http.serve.Resource;
import de.mobizcorp.femtocms.engine.EditEngine;
import de.mobizcorp.femtocms.engine.ViewEngine;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class RepositorySelector extends BasicResource {

    Logger logger = Logger.getLogger("de.mobizcorp.femtocms.httpd");

    protected static final String FCM_PREVIEW = "/preview:";

    private Resource editMount;

    private Resource saveMount;

    private Resource viewMount;

    private static final String logRequest = getString(LOG_REQUEST_PREFERENCE,
            LOG_REQUEST_FALLBACK);

    private static final String serverId = getString(FCM_SERVER_PREFERENCE,
            FCM_SERVER_FALLBACK);

    private static final Pattern writeAuth = createPattern(getString(
            WRITE_AUTH_PREFERENCE, WRITE_AUTH_FALLBACK));

    public RepositorySelector(Context context) {
        super(context);
        init(context);
    }

    private void init(Context ctx) {
        File base = new File(ctx.getBasePath());
        EditEngine edit = new EditEngine(base);
        this.editMount = new RepositoryMount(ctx, edit);
        this.saveMount = new RepositoryWriter(ctx, edit);
        ViewEngine view = new ViewEngine(base);
        this.viewMount = new RepositoryMount(ctx, view);
    }

    @Override
    protected void process(Request request, Response response) throws Exception {
        response.set("Server", serverId);
        String uri = request.getURI();
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, logRequest, new Object[] {
                    request.getInetAddress(), request.getMethod(), uri });
        }
        try {
            if (uri.startsWith(FCM_PREVIEW)) {
                authenticate(request);
                if ("POST".equals(request.getMethod())) {
                    saveMount.handle(request, response);
                } else {
                    request.setURI(uri.substring(FCM_PREVIEW.length()));
                    editMount.handle(request, response);
                }
            } else {
                viewMount.handle(request, response);
            }
        } catch (AccessControlException e) {
            handle(request, response, 403);
        } catch (SecurityException e) {
            response.set("WWW-Authenticate", "Basic realm="
                    + context.getBasePath());
            handle(request, response, 401);
        }
    }

    public static void authenticate(Request request) {
        Object attr = request.getAttributes().getAttribute("PeerPrincipal");
        if (attr instanceof Principal) {
            authenticate((Principal) attr);
        } else {
            MD5PasswordAuth.authenticate(request);
        }

    }

    private static void authenticate(Principal peer) {
        if (writeAuth == null || !writeAuth.matcher(peer.toString()).matches()) {
            throw new AccessControlException("permission denied");
        }
    }

    private static Pattern createPattern(String expr) {
        return expr == null ? null : Pattern.compile(expr);
    }

}
