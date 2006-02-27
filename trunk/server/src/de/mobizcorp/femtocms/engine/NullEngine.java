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
package de.mobizcorp.femtocms.engine;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.FCK_EDITOR_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.FCK_EDITOR_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;
import static de.mobizcorp.femtocms.resource.ResourceLoader.femtocmsLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;
import org.xml.sax.XMLFilter;

/**
 * This engine serves resources from the class path. It lso defines the shared
 * methods for the specilized engines.
 * 
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class NullEngine extends BaseEngine implements URIResolver {

    private static final File FCK_EDITOR_ARCHIVE = new File(getString(
            FCK_EDITOR_PREFERENCE, FCK_EDITOR_FALLBACK));

    protected final File base;

    private final SAXTransformerFactory tf;

    protected final URI baseUri;

    /**
     * The creation time of this engine, used as the modified time for resources
     * served from the class path. Note that this is incorrect in the event of a
     * hot code replace from the debugger, but this situation hardly is common
     * in production use.
     */
    protected final long mountTime = System.currentTimeMillis();

    protected final HashMap<String, String> methods = new HashMap<String, String>();

    private static final URLClassLoader fckLoader;
    static {
        URL[] urls;
        try {
            URL zip = FCK_EDITOR_ARCHIVE.toURL();
            urls = new URL[] { zip };
        } catch (MalformedURLException e) {
            urls = new URL[0];
            Logger.getLogger("de.mobizcorp.femtocms.engine").log(Level.SEVERE,
                    FCK_EDITOR_ARCHIVE.toString(), e);
        }
        fckLoader = new URLClassLoader(urls);
    }

    public NullEngine(File base) {
        if (!base.exists()) {
            throw new IllegalArgumentException("not found: " + base);
        }
        if (!base.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + base);
        }
        this.base = base;
        this.baseUri = base == null ? null : base.toURI();
        this.tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        this.tf.setURIResolver(this);
        methods.put("html", "femtocms:/media/html.xsl");
        methods.put("wml", "femtocms:/media/wml.xsl");
        methods.put("xml", "femtocms:/media/xml.xsl");
    }

    public Transformer newTransformer(String style) throws TransformerException {
        Templates templates = newTemplates(style);
        if (templates == null)
            return null;
        Transformer result = templates.newTransformer();
        configureTransformer(style, result);
        return result;
    }

    public XMLFilter newFilter(String style) throws TransformerException {
        Templates templates = newTemplates(style);
        if (templates == null)
            return null;
        TransformFilter result = new TransformFilter(tf
                .newTransformerHandler(templates));
        configureTransformer(style, result.getTransformer());
        return result;
    }

    public NullPipeline newPipeline(String method) throws TransformerException {
        String style = methods.get(method);
        if (style == null)
            return null;
        Templates templates = newTemplates(style);
        if (templates == null)
            return null;
        TransformerHandler handler = tf.newTransformerHandler(templates);
        configureTransformer(style, handler.getTransformer());
        return newPipeline(method, handler);
    }

    public NullPipeline newPipeline(String method, TransformerHandler handler) {
        return new ViewPipeline(this, handler);
    }

    protected void configureTransformer(String style, Transformer result) {
        result.setURIResolver(this);
        result.setParameter("femtocms-engine", this);
        result.setParameter("femtocms-href", style);
    }

    protected Templates newTemplates(String style) throws TransformerException {
        Source source = resolve(style);
        if (source == null) {
            return null;
        }
        Templates templates = tf.newTemplates(source);
        return templates;
    }

    public static String relativize(URI base, URI href) {
        // Must normalize first to avoid back-.. out of the context.
        href = href.normalize();
        URI relativeUri = base.relativize(href);
        if (!relativeUri.isAbsolute()) {
            return relativeUri.toString();
        } else {
            return href.toString();
        }
    }

    public long getLastModified(String href) {
        if (href.startsWith("fckeditor/")) {
            return FCK_EDITOR_ARCHIVE.lastModified();
        } else if (href.startsWith("femtocms:") || href.startsWith("femtocms/")) {
            return mountTime;
        } else if ("favicon.ico".equals(href)) {
            return EmptySource.INSTANCE.getLastModified();
        } else {
            return 0;
        }
    }

    public final StreamResource resolve(String href)
            throws TransformerException {
        try {
            return resolve(new URI(trimPath(href)));
        } catch (URISyntaxException e) {
            throw new TransformerException(href, e);
        }
    }

    public final Source resolve(String href, String base)
            throws TransformerException {
        try {
            URI hrefUri = new URI(trimPath(href));
            if (base != null && !hrefUri.isAbsolute()) {
                hrefUri = new URI(trimPath(base)).resolve(hrefUri);
            }
            return resolve(hrefUri);
        } catch (URISyntaxException e) {
            throw new TransformerException(href, e);
        }
    }

    private StreamResource resolve(URI uri) throws TransformerException {
        String href = relativize(this.baseUri, uri);
        try {
            StreamResource source = createStreamSource(href);
            if (source == null) {
                throw new ResourceNotFound(href);
            }
            return source;
        } catch (IOException e) {
            throw new ResourceNotFound(href + ": " + e);
        }
    }

    protected StreamResource createStreamSource(String href) throws IOException {
        URL resource;
        if (href.startsWith("femtocms:") || href.startsWith("femtocms/")) {
            String url = href.substring(9);
            url = trimPath(url);
            resource = femtocmsLoader.getResource(url);
        } else if (href.startsWith("fckeditor/")) {
            String url = href.substring(10);
            url = trimPath(url);
            int mark = url.indexOf('?');
            if (mark != -1)
                url = url.substring(0, mark);
            resource = fckLoader.getResource(url);
        } else if ("favicon.ico".equals(href)) {
            return EmptySource.INSTANCE;
        } else {
            return null;
        }
        if (resource == null) {
            return null;
        }
        StreamResource result = new StreamResource();
        result.setSystemId(href);
        URLConnection connection = resource.openConnection();
        result.setLastModified(connection.getDate());
        result.setInputStream(connection.getInputStream());

        return result;
    }

    public static String trimPath(String url) {
        int i = 0, end = url.length();
        while (i < end && url.charAt(i) == '/')
            i++;
        int q = url.indexOf('?', i);
        if (i == 0 && q == -1) {
            return url;
        } else {
            if (q == -1) {
                q = end;
            }
            return i < q ? url.substring(i, q) : "";
        }
    }

    public static Map<String, String> readAttributes(String data) {
        HashMap<String, String> result = new HashMap<String, String>();
        int mark = 0;
        int end = data == null ? 0 : data.length();
        while (mark < end) {
            int qual = data.indexOf('=', mark);
            if (qual == -1)
                break;
            int quot = data.indexOf('"', qual + 1);
            if (quot == -1) {
                quot = data.indexOf('\'', qual + 1);
                if (quot == -1)
                    break;
            }
            int stop = data.indexOf(data.charAt(quot), quot + 1);
            if (stop == -1)
                break;
            String name = data.substring(mark, qual).trim();
            String text = data.substring(quot + 1, stop);
            if (name.length() > 0) {
                result.put(name, text);
            }
            mark = stop + 1;
        }
        return result;
    }

    public String push(Node data) {
        Map<String, String> atts = readAttributes(data.getNodeValue());
        String type = atts.get("type");
        String href = atts.get("href");
        if (href == null || href.length() == 0) {
            return ""; // href is required - ignore for now.
        }
        if (type != null && type.endsWith("/css")) {
            return "<link rel=\"stylesheet\" type=\"" + type + "\" href=\""
                    + href + "\"/>";
        }
        return "";
    }

    public void refresh() {
    }

    public String modified() {
        return null;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public static void copy(Source source, OutputStream out) throws IOException {
        int n;
        byte[] buffer = new byte[8192];
        InputStream in = ((StreamSource) source).getInputStream();
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
    }

    public static String contentTypeFor(BasePipeline pipeline) {
        String value = pipeline.getOutputProperty("media-type");
        if (value != null) {
            return value;
        }
        value = pipeline.getOutputProperty("method");
        if (value == null || value.equals("text")) {
            return "text/plain";
        } else {
            return "text/" + value;
        }
    }
}
