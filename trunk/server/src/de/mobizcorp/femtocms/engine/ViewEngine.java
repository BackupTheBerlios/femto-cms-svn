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

import static de.mobizcorp.femtocms.prefs.ServerPreferences.HG_COMMAND_FALLBACK;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.HG_COMMAND_PREFERENCE;
import static de.mobizcorp.femtocms.prefs.ServerPreferences.getString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import de.mobizcorp.lib.ErrorStreamLogger;
import de.mobizcorp.lib.InputStreamEater;

import simple.util.cache.Cache; // FIXME

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class ViewEngine extends NullEngine {

    private static final int SHA1_LENGTH = 40;

    /**
     * Date format in the tip info. Sample:
     * <code>Sun Jul 24 07:56:01 2005</code>
     */
    private static final SimpleDateFormat tipDateFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss yyyy");

    private String tip = null;

    private String tipIndex = null;

    private long tipLastModified;

    private final HashMap<String, String> manifest;

    private final String hgCommand;

    private final Cache templateCache;

    public ViewEngine(File base) {
        super(base);
        File repos = new File(base, ".hg");
        if (!repos.exists() || !repos.isDirectory()) {
            throw new IllegalArgumentException("not a repository: " + base);
        }
        this.hgCommand = getString(HG_COMMAND_PREFERENCE, HG_COMMAND_FALLBACK);
        this.manifest = new HashMap<String, String>();
        this.templateCache = new Cache();
    }

    @Override
    public long getLastModified(String href) {
        try {
            href = relativize(this.baseUri, new URI(href));
            if (isInManifest(href)) {
                return tipLastModified;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return super.getLastModified(href);
        }
    }

    @Override
    protected StreamResource createStreamSource(String href) throws IOException {
        if (isInManifest(href)) {
            StreamResource result = new StreamResource();
            result.setLastModified(tipLastModified);
            result.setSystemId(baseUri.resolve(URI.create(href)).toString());
            result.setInputStream(execMercurial(null, "cat", href));
            return result;
        }
        return super.createStreamSource(href);
    }

    private void initManifest() throws IOException {
        String line;
        InputStream in;
        in = execMercurial(null, "tip");
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            Map<String, String> headers = readHeaders(reader);
            tip = headers.get("changeset");
            if (tip == null) {
                throw new IOException("no tip found, may not be a repository");
            } else {
                int colon = tip.indexOf(':');
                tipIndex = tip.substring(0, colon);
                try {
                    tipLastModified = tipDateFormat.parse(headers.get("date"))
                            .getTime();
                } catch (Exception e) {
                    tipLastModified = System.currentTimeMillis();
                }
                if (tipIndex.equals("-1")) {
                    // Nothing committed yet, empty repository.
                    return;
                }
            }
        } finally {
            in.close();
        }
        String oldTipIndex = manifest.get("");
        if (oldTipIndex != null && oldTipIndex.equals(tipIndex)) {
            return; // manifest is still valid - no need to reload.
        } else {
            manifest.clear();
            templateCache.clear();
            manifest.put("", tipIndex);
        }
        in = execMercurial(null, "manifest", tipIndex);
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                int mark1 = line.indexOf(' ');
                int mark2 = line.indexOf(' ', mark1 + 1);
                if (mark1 == SHA1_LENGTH && mark2 > mark1) {
                    String sha1 = line.substring(0, mark1);
                    String name = line.substring(mark2 + 1);
                    manifest.put(name, sha1);
                }
            }
            Logger.getLogger("de.mobizcorp.femtocms.engine").info(
                    "loaded manifest, " + manifest.size() + " entries for "
                            + tip);
        } finally {
            in.close();
        }
    }

    private boolean isInManifest(String href) throws IOException {
        if (tip == null) {
            initManifest();
        }
        return manifest.get(href) != null;
    }

    @Override
    protected void configureTransformer(String style, Transformer result) {
        super.configureTransformer(style, result);
        String sha1 = manifest.get(style);
        if (sha1 != null) {
            result.setParameter("femtocms-base", base.getPath());
            result.setParameter("femtocms-sha1", sha1);
        }
        result.setParameter("femtocms-tip", tip);
    }

    private InputStream execMercurial(String... args) throws IOException {
        args[0] = hgCommand;
        ProcessBuilder builder = new ProcessBuilder(args).directory(base);
        Process process = builder.start();
        ErrorStreamLogger.attach(process);
        return new InputStreamEater(process);
    }

    public String modified() {
        return tipDateFormat.format(new Date(tipLastModified));
    }

    public void refresh() {
        tip = null; // force manifest reload
    }

    private static Map<String, String> readHeaders(BufferedReader in)
            throws IOException {
        HashMap<String, String> result = new HashMap<String, String>();
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                break; // Header block end marker
            }
            int mark = line.indexOf(':');
            if (mark > 0) {
                String name = line.substring(0, mark).trim();
                String text = line.substring(mark + 1).trim();
                result.put(name, text);
            }
        }
        return result;
    }

    @Override
    protected Templates newTemplates(String style) throws TransformerException {
        Templates templates = (Templates) templateCache.lookup(style);
        if (templates != null) {
            return templates;
        }
        templates = super.newTemplates(style);
        templateCache.cache(style, templates);
        return templates;
    }

}
