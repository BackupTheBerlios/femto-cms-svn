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

import static de.mobizcorp.femtocms.prefs.ServerPreferences.WRITE_PASSWD_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.WRITE_PASSWD_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import simple.http.Request;
import simple.util.FileProperties;
import simple.util.net.Principal;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class MD5PasswordAuth {

    private static final Properties passwd = readPasswd(getString(
            WRITE_PASSWD_PREFERENCE, WRITE_PASSWD_FALLBACK));

    public static void authenticate(Request request) {
        if (isEnabled()) {
            Principal principal = request.getPrincipal();
            if (principal == null) {
                throw new SecurityException("not authenticated");
            }
            String hash = computeHash(principal);
            String want = passwd.getProperty(principal.getName());
            if (want != null && want.equals(hash)) {
                return; // OK
            } else {
                // Retry authentication
                throw new SecurityException("authentication failed");
            }
        } else {
            throw new AccessControlException("authentication failed");
        }
    }
    
    public static boolean isEnabled() {
        return passwd != null;
    }

    private static Properties readPasswd(String name) {
        try {
            if (name != null) {
                return new FileProperties(name);
            }
        } catch (IOException e) {
            Logger.getLogger("de.mobizcorp.femtocms.httpd").log(Level.SEVERE,
                    WRITE_PASSWD_PREFERENCE, e);
        }
        return null;
    }

    private static String computeHash(Principal principal) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String str = principal.getName() + "/" + principal.getPassword();
            return hex(md.digest(str.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    private static String hex(byte[] data) {
        int end = data == null ? 0 : data.length;
        StringBuffer buffer = new StringBuffer(end * 2);
        for (int i = 0; i < end; i++) {
            byte b = data[i];
            buffer.append(Integer.toHexString((b >>> 4) & 0xf));
            buffer.append(Integer.toHexString(b & 0xf));
        }
        return buffer.toString();
    }
}
