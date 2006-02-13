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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.transform.stream.StreamSource;

import simple.http.Request;
import simple.http.Response;
import simple.http.serve.BasicResource;
import simple.http.serve.Context;
import simple.util.net.Parameters;
import de.mobizcorp.femtocms.model.HuiFolder;
import de.mobizcorp.hui.HuiNode;
import de.mobizcorp.hui.HuiSource;
import de.mobizcorp.hui.Path;
import de.mobizcorp.hui.StateCodec;
import de.mobizcorp.lib.Text;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class RepositoryBrowser extends BasicResource {

    private final HuiSource source;
    private final HuiFolder handler;

    public RepositoryBrowser(Context ctx) throws IOException {
        super(ctx);
        HuiFolder.setBase(new File(ctx.getBasePath()));
        final URL hui = HuiFolder.class.getResource("FolderView.hui.xml");
        this.source = new HuiSource(new StreamSource(hui.openStream(), hui.toString()));
        this.handler = new HuiFolder();
    }

    @Override
    protected void process(Request request, Response response) throws IOException {
        Text url = Text.valueOf(request.getURI());
        int q = url.indexOf('?');
        if (q != -1) {
            url = url.part(0, q);
        }

        final OutputStream out = new BufferedOutputStream(response
                .getOutputStream());
        try {
            int slash = url.lastIndexOf('/');
            if (url.lastIndexOf('.') != -1) {
                final String name;
                if (slash != -1) {
                    name = url.part(slash + 1, url.size() - slash - 1)
                            .toString();
                } else {
                    name = url.toString();
                }
                InputStream file = HuiNode.class.getResourceAsStream(name);
                if (file != null) {
                    try {
                        byte[] data = new byte[8192];
                        int n;
                        while ((n = file.read(data)) != -1) {
                            if (n > 0) {
                                out.write(data, 0, n);
                            }
                        }
                    } finally {
                        file.close();
                    }
                    response.commit();
                } else {
                    handle(request, response, 404);
                }
                return;
            }
            HuiNode model = source.instance();
            if (slash != -1 && slash < url.size() - 1) {
                byte[] state = StateCodec.fromBase64(url.part(slash + 1,
                        url.size() - slash - 1).toBytes());
                model.readState(new ByteArrayInputStream(state));
            }
            final Parameters parameters = request.getParameters();
            final Enumeration i = parameters.getParameterNames();
            if (i.hasMoreElements()) {
                while (i.hasMoreElements()) {
                    final String name = i.nextElement().toString();
                    Path<HuiNode> path = model.path(Text.valueOf(name));
                    if (path != null) {
                    final String[] values = parameters.getParameters(name);
                    for (int j = 0; j < values.length; j++) {
                        path.getLast().post(Text.valueOf(values[j]), handler, path);
                    }}
                }

                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    model.writeState(buf);
                    response.set("Location", new String(buf.toByteArray(), "UTF-8"));
                    handle(request, response, 302);
                    return;
            } else {
                model.renderPage(out);
                response.commit();
            }
        } finally {
            out.close();
        }

        if (!response.isCommitted()) {
                    handle(request, response, 404);
                    return;
        }
    }

}
