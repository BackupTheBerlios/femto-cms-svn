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

import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.File;
import java.io.IOException;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.serve.Context;
import simple.http.serve.FileContext;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class RepositoryHandler implements ProtocolHandler {

    private final RepositorySelector selector;

    public RepositoryHandler(Context ctx) throws IOException {
        this.selector = new RepositorySelector(ctx);
    }

    public RepositoryHandler(String base) throws IOException {
        this(new FileContext(new File(base)));
    }

    public RepositoryHandler() throws IOException {
        this(getString(ROOT_MOUNT_PREFERENCE, ROOT_MOUNT_FALLBACK));
    }

    public void handle(Request request, Response response) {
        selector.handle(request, response);
    }

}
