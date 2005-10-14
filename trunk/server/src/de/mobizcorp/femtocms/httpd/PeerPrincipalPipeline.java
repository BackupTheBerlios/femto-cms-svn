package de.mobizcorp.femtocms.httpd;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.BUFFER_SIZE_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.BUFFER_SIZE_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getInt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import simple.http.Pipeline;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class PeerPrincipalPipeline extends Pipeline {

    private OutputStream out;

    private static final int BUFFER_SIZE = getInt(BUFFER_SIZE_PREFERENCE,
            BUFFER_SIZE_FALLBACK);

    public PeerPrincipalPipeline(Socket sock) throws IOException {
        super(sock);
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        return out != null ? out : (out = createOutputStream());
    }

    private OutputStream createOutputStream() throws IOException {
        OutputStream result = super.getOutputStream();
        if (BUFFER_SIZE > 0) {
            result = new BufferedOutputStream(result, BUFFER_SIZE);
        }
        return result;
    }

    @Override
    public synchronized Object getAttribute(String name) {
        if ("PeerPrincipal" == name) {
            try {
                if (sock instanceof SSLSocket) {
                    return ((SSLSocket) sock).getSession().getPeerPrincipal();
                }
            } catch (SSLPeerUnverifiedException e) {
                setAttribute("PeerException", e);
            }
            return null;
        } else {
            return super.getAttribute(name);
        }
    }

}
