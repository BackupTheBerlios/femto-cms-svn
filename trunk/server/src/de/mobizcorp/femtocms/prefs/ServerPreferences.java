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
package de.mobizcorp.femtocms.prefs;

import java.util.prefs.Preferences;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ServerPreferences {

    public static final String FCM_VERSION = "0.3";

    /**
     * The server identification string. Default: "femtocms/0.3".
     */
    public static final String FCM_SERVER_PREFERENCE = "server.id";

    public static final String FCM_SERVER_FALLBACK = "femtocms/0.3";

    /**
     * The size of the output buffer, to enable high TCP throughput. Default:
     * 2048.
     */
    public static final String BUFFER_SIZE_PREFERENCE = "buffer.size";

    public static final int BUFFER_SIZE_FALLBACK = 2048;

    /**
     * The expire factor is a multiplier to render time, which is added to the
     * current time to estimate an expires header value. Default: 100.
     */
    public static final String EXPIRE_FACTOR_PREFERENCE = "expire.factor";

    public static final double EXPIRE_FACTOR_FALLBACK = 100.0;

    /**
     * The FCK editor archive location. Resources for the editor will be loaded
     * directly from the distribution ZIP. Default: fck/FCKeditor_2.0.zip.
     */
    public static final String FCK_EDITOR_PREFERENCE = "fck.archive";

    public static final String FCK_EDITOR_FALLBACK = "fck/FCKeditor_2.0.zip";

    public static final String HG_COMMAND_PREFERENCE = "hg.command";

    public static final String HG_COMMAND_FALLBACK = "hg";

    /** This is the standard HTTP port. Default: 80. */
    public static final String HTTPD_PORT_PREFERENCE = "httpd.port";

    public static final int HTTPD_PORT_FALLBACK = 80;

    /** This is the SSL (HTTPS) port. Default: 443. */
    public static final String HTTPS_PORT_PREFERENCE = "https.port";

    public static final int HTTPS_PORT_FALLBACK = 443;

    /**
     * This is the SSL (HTTPS) PKCS#12 or JKS key and certificate file. Default:
     * ssl/localhost.jks.
     */
    public static final String HTTPS_AUTH_PREFERENCE = "https.auth";

    public static final String HTTPS_AUTH_FALLBACK = "ssl/localhost.jks";

    /**
     * This is the certificate file to authenticate users with. Default:
     * ssl/localhost.jks.
     */
    public static final String HTTPS_CERT_PREFERENCE = "https.cert";

    public static final String HTTPS_CERT_FALLBACK = "ssl/localhost.jks";

    /**
     * This is the SSL (HTTPS) key passphrase. Default: "".
     */
    public static final String HTTPS_PASS_PREFERENCE = "https.pass";

    public static final String HTTPS_PASS_FALLBACK = "";

    /**
     * Log request format. This is a message format for java.text.MessageFormat,
     * with the parameters peer IP, request method, request url. Default:
     * <code>{0} {1} {2}</code>.
     */
    public static final String LOG_REQUEST_PREFERENCE = "log.request";

    public static final String LOG_REQUEST_FALLBACK = "{0} {1} {2}";

    /** The root repository to mount. Default: current directory. */
    public static final String ROOT_MOUNT_PREFERENCE = "root.mount";

    public static final String ROOT_MOUNT_FALLBACK = ".";

    /** The output charset. Default: UTF-8. */
    public static final String OUTPUT_CHARSET_PREFERENCE = "output.charset";

    public static final String OUTPUT_CHARSET_FALLBACK = "UTF-8";

    /**
     * This is the key store type list, comma separated. Default:
     * "PKCS12,JKS,JCEKS".
     */
    public static final String STORE_TYPE_PREFERENCE = "store.type";

    public static final String STORE_TYPE_FALLBACK = "PKCS12,JKS,JCEKS";

    /**
     * A regular expression that must match against the distinguished name of
     * the peer principal to allow write access. This is checked against SSL
     * client certificates that can be verified against our own certificate key
     * store. Default: null.
     */
    public static final String WRITE_AUTH_PREFERENCE = "write.auth";

    public static final String WRITE_AUTH_FALLBACK = null;

    /**
     * A file containing MD5 passwords for write authentication. Default: null.
     */
    public static final String WRITE_PASSWD_PREFERENCE = "write.passwd";

    public static final String WRITE_PASSWD_FALLBACK = null;

    public static String getString(String preference, String fallback) {
        return Preferences.userNodeForPackage(ServerPreferences.class).get(
                preference, fallback);
    }

    public static int getInt(String preference, int fallback) {
        return Preferences.userNodeForPackage(ServerPreferences.class).getInt(
                preference, fallback);
    }

    public static double getDouble(String preference, double fallback) {
        return Preferences.userNodeForPackage(ServerPreferences.class)
                .getDouble(preference, fallback);
    }

    public static boolean getBoolean(String preference, boolean fallback) {
        return Preferences.userNodeForPackage(ServerPreferences.class)
                .getBoolean(preference, fallback);
    }
}
