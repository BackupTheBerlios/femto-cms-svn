package de.mobizcorp.femtocms.servlet;

import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.OUTPUT_CHARSET_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.ROOT_MOUNT_PREFERENCE;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import de.mobizcorp.femtocms.engine.BasePipeline;
import de.mobizcorp.femtocms.engine.NullEngine;
import de.mobizcorp.femtocms.engine.ResourceNotFound;
import de.mobizcorp.femtocms.engine.StreamResource;
import de.mobizcorp.femtocms.engine.ViewEngine;

/**
 * Servlet implementation class for Servlet: Presentation
 * 
 */
public class Presentation extends HttpServlet implements Servlet {

    private static final long serialVersionUID = -3510010237710751243L;

    private String charset = OUTPUT_CHARSET_FALLBACK;

    private ViewEngine engine;

    public Presentation() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        long ims = request.getDateHeader("If-Modified-Since");
        response.setDateHeader("Date", start);
        String value = request.getHeader("Pragma");
        if (value != null && value.equals("no-cache")) {
            engine.refresh();
        }
        String path = NullEngine.trimPath(request.getPathInfo());
        int dot = path.lastIndexOf('.');
        if (dot > 0 && !path.startsWith("fckeditor/")) {
            try {
                String type = path.substring(dot + 1);
                String href = path.substring(0, dot) + ".xml";
                if (ims != -1 && engine.getLastModified(href) <= ims) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                StreamResource text = getEngine().resolve(href);
                BasePipeline pipeline = getEngine().newPipeline(type);
                if (pipeline != null && text != null) {
                    pipeline.setParameter("femtocms-request-parameters",
                            request.getParameterMap());
                    response.setContentType(NullEngine.contentTypeFor(pipeline)
                            + ";charset=" + charset);
                    response.setDateHeader("Last-Modified", text
                            .getLastModified());
                    OutputStream out = response.getOutputStream();
                    try {
                        pipeline.setResult(new StreamResult(out));
                        pipeline.parse(SAXSource.sourceToInputSource(text));
                    } finally {
                        out.close();
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            } catch (ResourceNotFound e) {
                // Try to fallback, complain later.
            } catch (TransformerException e) {
                log("dynamic resource failure", e);
                throw new ServletException("transformer failure", e);
            } catch (SAXException e) {
                log("dynamic resource failure", e);
                throw new ServletException("transformer failure", e);
            } catch (RuntimeException e) {
                log("dynamic resource failure", e);
                throw e;
            }
        }

        if (!response.isCommitted()) {
            try {
                if (ims != -1 && getEngine().getLastModified(path) <= ims) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                StreamResource source = getEngine().resolve(path);
                if (source != null) {
                    if (ims != -1 && source.getLastModified() <= ims) {
                        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                    response.setDateHeader("Last-Modified", source
                            .getLastModified());
                    OutputStream out = response.getOutputStream();
                    NullEngine.copy(source, out);
                    out.close();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Not found: '" + path + "'");
                    return;
                }
            } catch (ResourceNotFound e) {
                log("static resource failure", e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Not found: '" + e + "'");
                return;
            } catch (TransformerException e) {
                log("static resource failure", e);
                throw new ServletException("transformer failure", e);
            } catch (RuntimeException e) {
                log("static resource failure", e);
                throw e;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        try {
            String path = NullEngine.trimPath(request.getPathInfo());
            return getEngine().getLastModified(path);
        } catch (IOException e) {
            return super.getLastModified(request);
        }
    }

    private ViewEngine getEngine() throws IOException {
        if (engine == null) {
            final String rootMount = getInitParameter(ROOT_MOUNT_PREFERENCE);
            engine = new ViewEngine(new File(
                    rootMount == null ? ROOT_MOUNT_FALLBACK : rootMount));
        }
        return engine;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String initCharset = config.getInitParameter(OUTPUT_CHARSET_PREFERENCE);
        if (initCharset != null && initCharset.length() > 0) {
            charset = initCharset;
        }
    }
}