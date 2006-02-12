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
        if (report.getCode() != 304) {
            // Do not complain for not modified reports.
            Logger.getLogger("de.mobizcorp.femtocms.httpd").warning(
                    report.getText() + " " + report.getCause());
        }
        super.process(req, resp, report);
    }
}
