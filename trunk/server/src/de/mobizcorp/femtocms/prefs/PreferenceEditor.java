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
 * PreferenceEditor is a simple command line editor for femtocms preferences.
 * Synopsis:<br/>
 *   femtocms-prefs { <var>key</var> {-print|-default|-value <var>value</var>} ...}
 * Options:
 * <ul>
 * <li>-print prints the current preference setting</li>
 * <li>-default resets the preference to default</li>
 * <li>-value sets the preference value to the next argument</li>
 * </ul>
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class PreferenceEditor {
    public static void main(String args[]) {
        String current = null;
        boolean putNext = false;
        Preferences preferences = Preferences
                .userNodeForPackage(ServerPreferences.class);
        try {
            for (String arg : args) {
                if (arg.equals("-value")) {
                    putNext = true;
                } else if (arg.equals("-print")) {
                    putNext = false;
                    System.out.print(current);
                    System.out.print("=");
                    System.out.println(preferences.get(current, ""));
                } else if (arg.equals("-default")) {
                    putNext = false;
                    preferences.remove(current);
                } else if (arg.startsWith("-")) {
                    throw new IllegalArgumentException("unknown option: " + arg);
                } else if (putNext) {
                    putNext = false;
                    preferences.put(current, arg);
                } else {
                    current = arg;
                }
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(-1);
        }
    }
}
