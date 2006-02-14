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

import static de.mobizcorp.femtocms.prefs.ServerPreferences.FCM_VERSION;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPD_PORT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPD_PORT_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_AUTH_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_AUTH_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_CERT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_CERT_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_PASS_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_PASS_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_PORT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HTTPS_PORT_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.STORE_TYPE_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.STORE_TYPE_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getInt;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;
import de.mobizcorp.lib.ErrorStreamLogger;
import de.mobizcorp.lib.InputStreamEater;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ServerMain {
    public static void main(String[] args) {
        splash();
        try {
            Connection connection = ConnectionFactory
                    .getConnection(new RepositoryHandler(),
                            new PeerPrincipalPipelineFactory());

            int httpdPort = getInt(HTTPD_PORT_PREFERENCE, HTTPD_PORT_FALLBACK);
            if (httpdPort > 0) {
                connection.connect(new ServerSocket(httpdPort));
                System.out.println("HTTPD bound to " + httpdPort);
            }

            int httpsPort = getInt(HTTPS_PORT_PREFERENCE, HTTPS_PORT_FALLBACK);
            String httpsCert = getString(HTTPS_CERT_PREFERENCE,
                    HTTPS_CERT_FALLBACK);
            if (httpsPort > 0 && httpsCert != null
                    && new File(httpsCert).isFile()) {
                KeyStore store;
                char[] httpsPass = toChars(getString(HTTPS_PASS_PREFERENCE,
                        HTTPS_PASS_FALLBACK));
                try {
                    store = loadStore(httpsCert, httpsPass);
                } catch (GeneralSecurityException e) {
                    char[] pass = readPass("Certificate store password for "
                            + httpsCert + ": ");
                    try {
                        store = loadStore(httpsCert, pass);
                    } finally {
                        stomp(pass);
                    }
                }
                KeyManagerFactory keyFactory = KeyManagerFactory
                        .getInstance(KeyManagerFactory.getDefaultAlgorithm());
                try {
                    keyFactory.init(store, httpsPass);
                } catch (UnrecoverableKeyException e) {
                    char[] pass = readPass("Certificate key password for "
                            + httpsCert + ": ");
                    try {
                        keyFactory.init(store, pass);
                    } finally {
                        stomp(pass);
                    }
                }
                TrustManagerFactory trustFactory = null;
                String httpsAuth = getString(HTTPS_AUTH_PREFERENCE,
                        HTTPS_AUTH_FALLBACK);
                if (httpsAuth != null && httpsAuth.length() > 0) {
                    KeyStore trustStore = loadStore(httpsAuth, null);
                    trustFactory = TrustManagerFactory.getInstance("PKIX");
                    trustFactory.init(trustStore);
                }
                SSLContext sslContext = SSLContext.getInstance("SSLv3");
                sslContext.init(keyFactory.getKeyManagers(),
                        trustFactory == null ? null : trustFactory
                                .getTrustManagers(), null);
                ServerSocket socket = sslContext.getServerSocketFactory()
                        .createServerSocket(httpsPort);
                SSLServerSocket sslSocket = ((SSLServerSocket) socket);
                sslSocket.setWantClientAuth(trustFactory != null);
                sslSocket.setNeedClientAuth(trustFactory != null
                        && !MD5PasswordAuth.isEnabled());
                connection.connect(socket);
                System.out.println("HTTPS bound to " + httpsPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static KeyStore loadStore(String path, char[] pass)
            throws GeneralSecurityException {
        StringTokenizer tok = new StringTokenizer(getString(
                STORE_TYPE_PREFERENCE, STORE_TYPE_FALLBACK), ",; \t\n\r");
        GeneralSecurityException failure = null;
        while (tok.hasMoreTokens()) {
            KeyStore store;
            FileInputStream in = null;
            try {
                store = KeyStore.getInstance(tok.nextToken());
                in = new FileInputStream(path);
                store.load(in, pass != null && pass.length > 0 ? pass : null);
                return store;
            } catch (IOException e) {
                failure = new KeyStoreException(path, e);
            } catch (GeneralSecurityException e) {
                failure = e;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignored
                    }
                }
            }
        }
        if (failure == null) {
            throw new KeyStoreException("no key store type");
        } else {
            throw failure;
        }
    }

    private static char[] readPass(String prompt) throws IOException,
            InterruptedException {
        char[] buffer = new char[20];
        echoOff();
        try {
            System.out.print(prompt);
            System.out.flush();
            int count = 0, c;
            while ((c = System.in.read()) != -1) {
                if (c == '\n' || c == '\r') {
                    break;
                }
                if (count >= buffer.length) {
                    buffer = resize(buffer, buffer.length * 2);
                }
                buffer[count++] = (char) c;
            }
            System.out.println();
            if (buffer.length != count) {
                buffer = resize(buffer, count);
            }
            return buffer;
        } finally {
            echoOn();
        }
    }

    private static char[] resize(char[] buffer, int size) {
        char[] copy = new char[size];
        int rest = size < buffer.length ? size : buffer.length;
        System.arraycopy(buffer, 0, copy, 0, rest);
        stomp(buffer);
        return copy;
    }

    private static char[] toChars(String pass) {
        return pass != null && pass.length() > 0 ? pass.toCharArray() : null;
    }

    private static void stomp(char[] buffer) {
        if (buffer != null) {
            Arrays.fill(buffer, (char) 0);
        }
    }

    private static void echoOff() throws IOException, InterruptedException {
        runCommand(new ProcessBuilder("stty", "-F", "/dev/tty", "-echo"));
    }

    private static void echoOn() throws IOException, InterruptedException {
        runCommand(new ProcessBuilder("stty", "-F", "/dev/tty", "echo"));
    }

    private static void runCommand(ProcessBuilder builder) throws IOException,
            InterruptedException {
        Process process = builder.start();
        ErrorStreamLogger logger = ErrorStreamLogger.attach(process);
        new InputStreamEater(process).close();
        if (logger.getExitCode() != 0) {
            throw new IOException("error executing: " + builder.command());
        }
    }

    private static void splash() {
        System.out
                .println("femtocms, version " + FCM_VERSION + ", unreleased.");
        System.out.println("No warranty; not even for merchantability"
                + " or fitness for a perticular purpose.");
        System.out.println("Rendering Copyright (C) 2005-2006"
                + " mobizcorp Europe Ltd., all rights reserved.");
        System.out.println("Simpleweb Copyright (C) 2001 Niall Gallagher,"
                + " subject to the GNU Lesser General Public License.");
        System.out.println("Mercurial Copyright (C) 2005 Matt Mackall,"
                + " subject to the GNU General Public License.");
        System.out
                .println("FCKeditor Copyright (C) 2003-2005 Frederico Caldeira Knabben,"
                        + " subject to the GNU Lesser General Public License.");
    }
}
